package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.traits.Addition
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FusedMultiplyAdd
import com.kelvsyc.kotlin.core.Multiplication
import com.kelvsyc.kotlin.core.traits.Signed

/**
 * Implementation of multiplication and division operations for doubled floating-point types.
 *
 * @param D The doubled floating-point type
 * @param F the underlying floating-point type
 */
@Suppress("detekt:TooManyFunctions")
abstract class AbstractDoubleFloatingPointMultiplication<F, D : DoubleFloatingPoint<F>> : Multiplication<D> {
    /**
     * Object providing basic trait information about the underlying floating-point type.
     */
    protected abstract val traits: FloatingPoint<F>

    protected abstract val baseAddition: Addition<F>
    protected abstract val baseMultiplication: Multiplication<F>
    protected abstract val signed: Signed<F>

    /**
     * Object providing a fused multiply add operation on the underlying floating-point type.
     *
     * This is strictly optional, and the functions here have alternatives that work without FMA. The default assumes
     * that FMA is not available.
     */
    protected open val fma: FusedMultiplyAdd<F>? = null

    protected abstract val addition: AbstractDoubleFloatingPointAddition<F, D>

    protected abstract fun create(high: F, low: F): D

    // 2^(ceil(sizeBits / 2)) + 1
    protected abstract val splitPoint: F

    /**
     * Splits the specified floating-point value into two floating-point values, a high and low value, such that the
     * two values do not overlap and add exactly to the original value. Each value has roughly half the bits of the
     * original, with the high value having a larger magnitude.
     *
     * This is generally used in doubled floating point multiplication algorithms.
     */
    fun split(value: F): Pair<F, F> {
        // This split algorithm can actually be more generic, given that the split point can be anywhere from
        // s = (sizeBits / 2) to (sizeBits - 1), but we only need it to be ceil(sizeBits/2) in practice
        // The split point is defined as 2^s + 1
        val c = baseMultiplication.multiply(splitPoint, value)
        val big = baseAddition.subtract(c, value)
        val high = baseAddition.subtract(c, big)
        val low = baseAddition.subtract(value, high)
        return high to low
    }

    /**
     * Multiplies two floating-point values.
     *
     * The value returned is considered an exact product, with [high][DoubleFloatingPoint.high] considered an
     * approximation of the true product and [low][DoubleFloatingPoint.low] considered a round-off error therein.
     */
    fun twoProduct(a: F, b: F): D {
        if (fma != null) {
            // If FMA is available, then we can get the necessary prevision pretty easily
            val s = baseMultiplication.multiply(a, b)
            val e = fma!!.fma(a, b, signed.negate(s))
            return create(s, e)
        } else {
            // This is the algorithm used when FMA is not available.
            val s = baseMultiplication.multiply(a, b)
            val (aHigh, aLow) = split(a)
            val (bHigh, bLow) = split(b)
            val err1 = baseAddition.subtract(s, baseMultiplication.multiply(aHigh, bHigh))
            val err2 = baseAddition.subtract(err1, baseMultiplication.multiply(aLow, bHigh))
            val err3 = baseAddition.subtract(err2, baseMultiplication.multiply(aHigh, bLow))
            val e = baseAddition.subtract(baseMultiplication.multiply(aLow, bLow), err3)
            return create(s, e)
        }
    }

    /**
     * Multiplies a scalar [b] to a number represented by an ordered list of components [a]. The nonzero components of
     * [a] must be sorted with the least significant component first; zeroes may be freely interspersed among the
     * components.
     *
     * This is generally useful in advanced adaptive-precision use cases, where [a] consists of 3 or more components
     * (ie. `TripleDouble`, `QuadrupleDouble`, etc.)
     *
     * The returned list will have twice the components of [a], all non-overlapping. Note that the returned value may
     * contain spurious zero components scattered throughout, but all nonzero components will be sorted with the least
     * significant component first.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun scaleExpansion(a: List<F>, b: F): List<F> {
        require(a.isNotEmpty()) { "Cannot product with empty lhs" }

        return buildList {
            var q = twoProduct(a.first(), b)
            add(q.low)
            for (i in 1 ..< a.size) {
                val t = twoProduct(a[i], b)
                q = addition.twoSum(q, t.low)
                add(q.low)
                q = addition.fastTwoSum(t.high, q.high)
                add(q.low)
            }
            add(q.high)
        }
    }

    /**
     * Multiplies two numbers represented by an ordered list of components. The nonzero elements of both numbers must be
     * sorted with the least significant component first; zeroes may be freely interspersed among the components.
     *
     * This is generally useful in advanced adaptive-precision use cases, where numbers may consist of 3 or more
     * components (ie. `TripleDouble`, `QuadrupleDouble`, etc.)
     *
     * The returned list will have a [size][List.size] equal to the product of the two list sizes. Note that the
     * returned value may contain spurious zero components scattered throughout, but all nonzero components will be
     * sorted with the least significant component first.
     */
    fun expansionProduct(a: List<F>, b: List<F>): List<F> {
        require(a.isNotEmpty()) { "Cannot product with empty lhs" }
        require(b.isNotEmpty()) { "Cannot product with empty rhs" }

        // We are basically doing a naive multiplication by treating each component of b as a scalar, producing a list
        // of scaleExpansion() products, which we distill to produce the sum.
        return addition.distill(b.flatMap { scaleExpansion(a, it) })
    }

    /**
     * Multiplies a doubled value to a scalar value.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `2u^2` if FMA is available, and at most
     * `3u^2` if FMA is not available.
     */
    fun twoProduct(a: D, b: F): D {
        if (fma != null) {
            val c = twoProduct(a.high, b)
            val c2 = fma!!.fma(a.low, b, c.low)
            return addition.fastTwoSum(c.high, c2)
        } else {
            // This method is slightly faster than unrolling scaleExpansion(listOf(a.low, a.high), b)
            // There's another algorithm that has one last fast2Sum() call but is less accurate.
            val c = twoProduct(a.high, b)
            val c2 = baseMultiplication.multiply(a.low, b)
            val t = addition.fastTwoSum(c.high, c2)
            val t2 = baseAddition.add(t.low, c.low)
            return addition.fastTwoSum(t.high, t2)
        }
    }

    /**
     * Multiplies two doubled values.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `5u^2` if FMA is available, and at most
     * `7u^2` if FMA is not available.
     */
    fun twoProduct(a: D, b: D): D {
        val c = twoProduct(a.high, b.high)
        val c2 = if (fma != null) {
            // The version with FMA is more accurate in part because we do integrate a.low * b.low in our computation.
            val t0 = baseMultiplication.multiply(a.low, b.low)
            val t1 = fma!!.fma(a.high, b.low, t0)
            fma!!.fma(a.low, b.high, t1)
        } else {
            // This multiplication effectively drops a.low * b.low to provide a reasonable approximation.
            val t1 = baseMultiplication.multiply(a.high, b.low)
            val t2 = baseMultiplication.multiply(a.low, b.high)
            baseAddition.add(t1, t2)
        }
        val c3 = baseAddition.add(c.low, c2)
        return addition.fastTwoSum(c.high, c3)
    }

    /**
     * Divides a doubled value by a scalar value.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `3u^2`.
     */
    fun twoDivide(a: D, b: F): D {
        // Division can't ever be exact, and so we are implementing a naive long division algorithm here.
        val c = baseMultiplication.divide(a.high, b)
        val p = twoProduct(c, b)
        val dh = baseAddition.subtract(a.high, p.high)
        val dt = baseAddition.subtract(dh, p.low)
        val d = baseAddition.add(dt, a.low)
        val t = baseMultiplication.divide(d, b)
        return addition.fastTwoSum(c, t)
    }

    /**
     * Divides a doubled value by another doubled value.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `9.8u^2` if FMA is available, and
     * at most `15u^2 + 56u^3` if FMA is not available. Do note that if FMA is available, this function will use a
     * slower but more accurate algorithm to obtain the smaller error bound.
     */
    fun twoDivide(a: D, b: D): D {
        // Division can't ever be exact, and so we are implementing a naive long division algorithm here.
        if (fma != null) {
            // With FMA, we have a more accurate operation, but uses double the number operations
            val th = baseMultiplication.divide(traits.one, b.high)
            val rh = baseAddition.subtract(traits.one, baseMultiplication.multiply(b.high, th))
            val rl = signed.negate(baseMultiplication.multiply(b.low, th))
            val e = addition.fastTwoSum(rh, rl)
            val d = twoProduct(e, th)
            val m = addition.twoSum(d, th)
            return twoProduct(a, m)
        } else {
            // Without FMA, we have an operation that is less accurate, but is faster
            val c = baseMultiplication.divide(a.high, b.high)
            val p = twoProduct(b, c)
            val dh = baseAddition.subtract(a.high, p.high)
            val dt = baseAddition.subtract(a.low, p.low)
            val d = baseAddition.add(dh, dt)
            val t = baseMultiplication.divide(d, b.high)
            return addition.fastTwoSum(c, t)
        }
    }

    override fun multiply(lhs: D, rhs: D): D = twoProduct(lhs, rhs)
    override fun divide(lhs: D, rhs: D): D = twoDivide(lhs, rhs)
}
