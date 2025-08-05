package com.kelvsyc.kotlin.core.fp.traits

import com.kelvsyc.kotlin.core.fp.AbstractDoubleFloatingPointAddition
import com.kelvsyc.kotlin.core.fp.AbstractDoubleFloatingPointMultiplication
import com.kelvsyc.kotlin.core.fp.DoubleFloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.FusedMultiplyAdd

/**
 * Implementation of [DoubleFloatingPointDivision] based on a naive long division algorithm.
 *
 * This implementation supports the use of an optional [FusedMultiplyAdd] trait, which tightens the relative error from
 * exact for division operations.
 */
class DoubleFloatingPointLongDivision<F, D : DoubleFloatingPoint<F>>(
    private val baseTraits: FloatingPoint<F>,
    private val baseArithmetic: FloatingPointArithmetic<F>,
    private val addition: AbstractDoubleFloatingPointAddition<F, D>, // TODO Replace with interface
    private val multiplication: AbstractDoubleFloatingPointMultiplication<F, D>, // TODO Replace with interface
    private val fma: FusedMultiplyAdd<F>? = null
) : DoubleFloatingPointDivision<F, D> {
    /**
     * Divides a doubled value by a scalar value.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `3u^2`.
     */
    override fun divide(lhs: D, rhs: F): D {
        val c = baseArithmetic.divide(lhs.high, rhs)
        val p = multiplication.twoProduct(c, rhs)
        val dh = baseArithmetic.subtract(lhs.high, p.high)
        val dt = baseArithmetic.subtract(dh, p.low)
        val d = baseArithmetic.add(dt, lhs.low)
        val t = baseArithmetic.divide(d, rhs)
        return addition.fastTwoSum(c, t)
    }

    /**
     * Divides a doubled value by another doubled value.
     *
     * The relative error from exact should be, if `u = 0.5 * ulp(1)^2`, at most `9.8u^2` if FMA is available, and
     * at most `15u^2 + 56u^3` if FMA is not available. Do note that if FMA is available, this function will use a
     * slower but more accurate algorithm to obtain the smaller error bound.
     */
    override fun divide(lhs: D, rhs: D): D {
        if (fma != null) {
            // With FMA, we have a more accurate operation, but uses double the number operations
            val th = baseArithmetic.divide(baseTraits.one, rhs.high)
            val rh = baseArithmetic.subtract(baseTraits.one, baseArithmetic.multiply(rhs.high, th))
            val rl = baseTraits.negate(baseArithmetic.multiply(rhs.low, th))
            val e = addition.fastTwoSum(rh, rl)
            val d = multiplication.twoProduct(e, th)
            val m = addition.twoSum(d, th)
            return multiplication.twoProduct(lhs, m)
        } else {
            // Without FMA, we have an operation that is less accurate, but is faster
            val c = baseArithmetic.divide(lhs.high, rhs.high)
            val p = multiplication.twoProduct(rhs, c)
            val dh = baseArithmetic.subtract(lhs.high, p.high)
            val dt = baseArithmetic.subtract(lhs.low, p.low)
            val d = baseArithmetic.add(dh, dt)
            val t = baseArithmetic.divide(d, rhs.high)
            return addition.fastTwoSum(c, t)
        }
    }
}
