package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.Addition
import com.kelvsyc.kotlin.core.FloatingPoint

/**
 * Implementation of addition and subtraction operations for doubled floating-point types.
 *
 * @param D The doubled floating-point type
 * @param F The underlying floating-point type
 */
@Suppress("detekt:TooManyFunctions")
abstract class AbstractDoubleFloatingPointAddition<F, D : DoubleFloatingPoint<F>> : Addition<D> {
    /**
     * Object providing the addition and subtraction operations on the underlying floating-point type.
     */
    protected abstract val base: Addition<F>

    /**
     * Object providing basic trait information about the underlying floating-point type.
     */
    protected abstract val traits: FloatingPoint<F>

    /**
     * Object comparing two instances of the underlying floating-point type.
     */
    protected abstract val comparator: Comparator<F>

    /**
     * Object providing signed operations on the doubled floating-point type.
     */
    protected abstract val signed: DoubleFloatingPoint.Signed<F, D>

    protected abstract fun create(high: F, low: F): D

    /**
     * Adds two floating-point values.
     *
     * The value [a] is required to have the same or larger magnitude than that of [b]. The value returned is considered
     * an exact sum, with [high][DoubleFloatingPoint.high] considered an approximation of the true sum and [low][DoubleFloatingPoint.low]
     * considered a round-off error therein.
     *
     * @param a The larger of the two floating-point values
     * @param b The smaller of the two floating-point values
     */
    fun fastTwoSum(a: F, b: F): D {
        // TODO Handle one or both inputs NaN
        val s = base.add(a, b)
        if (traits.isInfinite(s)) {
            // If the base sum is infinite, we should go no further
            return create(s, traits.zero)
        }
        val bVirt = base.subtract(s, a)
        val e = base.subtract(b, bVirt)
        return create(s, e)
    }

    /**
     * Adds two floating-point values.
     *
     * The value returned is considered an exact sum, with [high][DoubleFloatingPoint.high] considered an approximation
     * of the true sum and [low][DoubleFloatingPoint.low] considered a round-off error therein.
     *
     * Unlike [fastTwoSum], there is no requirement that one argument be larger than the other.
     */
    fun twoSum(a: F, b: F): D {
        // TODO Handle one or both inputs NaN
        val s = base.add(a, b)
        if (traits.isInfinite(s)) {
            // If the base sum is infinite, we should go no further
            return create(s, traits.zero)
        }
        val (aVirt, bVirt) = base.subtract(s, a).let {
            // FIXME Sometimes it is an infinity - have to figure out why
            if (traits.isInfinite(it)) {
                base.subtract(s, b).let { it to base.subtract(s, it) }
            } else {
                base.subtract(s, it) to it
            }
        }
        val aRound = base.subtract(a, aVirt)
        val bRound = base.subtract(b, bVirt)
        val e = base.add(aRound, bRound)
        return create(s, e)
    }

    /**
     * Adds a scalar [b] to a number represented by an ordered list of components [a]. The nonzero components of [a]
     * must be sorted with the least significant component first; zeroes may be freely interspersed among the
     * components.
     *
     * This is generally useful in advanced adaptive-precision use cases, where [a] consists of 3 or more components
     * (ie. `TripleDouble`, `QuadrupleDouble`, etc.)
     *
     * The returned list will have `a.`[size][List.size]` + 1` components. Note that the returned value may contain
     * spurious zero components scattered throughout, but all nonzero components will be sorted with the least
     * significant component first.
     */
    fun growExpansion(a: List<F>, b: F): List<F> = buildList {
        var q = b   // Each time q is written, we have an approximation of b + the first n terms of a
        a.forEach {
            val sum = twoSum(q, it)
            add(sum.low)
            q = sum.high
        }
        add(q) // This last term is thus a reasonable approximation of the sum, and thus the most significant component
    }

    /**
     * Adds two numbers represented by an ordered list of components. The nonzero elements of both numbers must be
     * sorted with the least significant component first; zeroes may be freely interspersed among the components.
     *
     * This is generally useful in advanced adaptive-precision use cases, where numbers may consist of 3 or more
     * components (ie. `TripleDouble`, `QuadrupleDouble`, etc.)
     *
     * The returned list will have a [size][List.size] equal to the combined total of the two lists. Note that the
     * returned value may contain spurious zero components scattered throughout, but all nonzero components will be
     * sorted with the least significant component first.
     */
    fun expansionSum(a: List<F>, b: List<F>): List<F> = buildList {
        // The EXPANSION-SUM algorithm essentially adds each component of b to a, one at a time, dropping the least
        // significant component of the result each time into the sum.
        var q = a
        b.forEach {
            val sum = growExpansion(q, it)
            add(sum.first())
            q = sum.drop(1)
        }
        addAll(q)
    }

    /**
     * Adds two numbers represented by an ordered list of components. The nonzero elements of both numbers must be
     * sorted with the least significant component first; zeroes may be freely interspersed among the components.
     * Both numbers must also be expressed in terms of "strongly nonoverlapping" components.
     *
     * A list of components is "strongly nonoverlapping" if:
     * * no two components are overlapping
     * * each component is adjacent to at most one other component
     * * if two components are adjacent, both have one-bit significands (ie. both are powers of 2)
     * (Note that by definition, a list of nonadjacent components is thus strongly nonoverlapping.)
     *
     * If we have the strongly overlapping property, this function will be faster than [expansionSum] in the sense that
     * it will compute the sum in fewer [twoSum]/[fastTwoSum] operations. If one number is a single component, however,
     * [growExpansion] should be used instead. Generally, the performance between this function and [expansionSum] will
     * be minimal unless one or both numbers have large numbers of components.
     *
     * The returned list will have a [size][List.size] equal to the combined total of the two lists. Note that the
     * returned value may contain spurious zero components scattered throughout, but all nonzero components will be
     * sorted with the least significant component first. Additionally, the returned value will have strongly
     * nonoverlapping components
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun fastExpansionSum(a: List<F>, b: List<F>): List<F> {
        require(a.isNotEmpty()) { "Cannot sum with empty lhs" }
        require(b.isNotEmpty()) { "Cannot sum with empty rhs" }

        // First, we merge the two lists
        val g = buildList {
            val ait = a.iterator()
            val bit = b.iterator()
            var aNext: F? = null
            var bNext: F? = null
            while ((aNext != null || ait.hasNext()) && (bNext != null || bit.hasNext())) {
                if (aNext == null) aNext = ait.next()
                if (bNext == null) bNext = bit.next()

                // Add the smaller of the two by magnitude
                val cmp = comparator.compare(signed.base.absoluteValue(aNext!!), signed.base.absoluteValue(bNext!!))
                if (cmp > 0) {
                    add(bNext)
                    bNext = null
                } else {
                    add(aNext)
                    aNext = null
                }
            }
            if (ait.hasNext()) {
                addAll(ait.asSequence())
            }
            if (bit.hasNext()) {
                addAll(bit.asSequence())
            }
        }

        val result = buildList {
            var q = fastTwoSum(g[1], g[0])
            add(q.low)
            for (i in 2 ..< g.size) {
                q = twoSum(q.high, g[i])
                add(q.low)
            }
            add(q.high)
        }
        return result
    }

    /**
     * Adds a doubled value to a scalar value.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `2u^2`.
     */
    fun twoSum(a: D, b: F): D {
        // This is basically growExpansion(listOf(a.low, a.high), b), but unrolled for efficiency
        val s0 = twoSum(a.low, b)
        val s1 = twoSum(a.high, s0.high)

        // The problem is that sumElements is a sequence of three elements, and we only need two
        // This addition introduces error due to dropping that last element
        val t0 = fastTwoSum(s1.low, s0.low)
        val t1 = fastTwoSum(s1.high, t0.high)
        return fastTwoSum(t1.high, base.add(t1.low, t0.low))
    }

    /**
     * Adds two doubled values.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `3u^2 + 13u^3`.
     */
    fun twoSum(a: D, b: D): D {
        // This is basically expansionSum(listOf(a.low, a.high), listOf(b.low, b.high), but unrolled for efficiency
        val s0 = twoSum(a.low, b.low)
        val s11 = twoSum(a.high, s0.high)
        val s12 = twoSum(s11.low, b.high)
        val s2 = twoSum(s11.high, s12.high)

        // This results in, in this case, 4 components that make up the exact sum
        // From most significant to least: s2.high, s2.low, s12.low, and s0.low
        // Now we have to compact it into two, which introduces error
        val t0 = fastTwoSum(s2.low, s12.low)
        val t1 = fastTwoSum(t0.low, s0.low)
        val t2 = fastTwoSum(t0.high, t1.high)
        val t3 = fastTwoSum(s2.high, t2.high)
        return fastTwoSum(t3.high, base.add(base.add(t1.low, t2.low), t3.low))
    }

    /**
     * Adds a list of values exactly. The supplied list does not need to be ordered.
     *
     * The value returned can be treated as a single value of components the size of the input, with all nonzero values
     * being ordered from least to most significant.
     */
    fun distill(value: List<F>): List<F> {
        return when (value.size) {
            0 -> emptyList()
            1 -> value
            2 -> {
                val result = twoSum(value[0], value[1])
                listOf(result.low, result.high)
            }
            else -> {
                val half = value.size / 2
                val left = value.subList(0, half)
                val right = value.subList(half, value.size - 1)
                val leftResult = distill(left)
                val rightResult = distill(right)
                expansionSum(leftResult, rightResult)
            }
        }
    }

    override fun add(lhs: D, rhs: D): D = twoSum(lhs, rhs)
    override fun subtract(lhs: D, rhs: D): D = twoSum(lhs, signed.negate(rhs))
}
