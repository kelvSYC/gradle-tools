package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.DoubleBits
import com.kelvsyc.kotlin.core.TypeTraits
import com.kelvsyc.kotlin.core.fp.traits.DoubleFloatingPointDivision
import com.kelvsyc.kotlin.core.fp.traits.DoubleFloatingPointLongDivision
import kotlin.math.absoluteValue
import com.kelvsyc.kotlin.core.traits.Signed as BaseSigned

/**
 * `DoubleDouble` is a "double word" number, extending [Double] by using a second non-overlapping [Double] to extend the
 * precision of the first, though not to the extent of the precision offered vy a `binary128` (quad-precision)
 * floating-point type.
 */
class DoubleDouble private constructor(
    override val high: Double, override val low: Double
) : DoubleFloatingPoint<Double> {
    companion object {
        fun of(value: Double) = DoubleDouble(value, 0.0)
    }

    object Signed : DoubleFloatingPoint.Signed<Double, DoubleDouble> {
        override val base: BaseSigned<Double>
            get() = TypeTraits.Double

        override fun isNegative(value: DoubleDouble): Boolean =
            value.high < 0.0 || (value.high == 0.0 && value.low < 0.0)
        override fun isPositive(value: DoubleDouble): Boolean =
            value.high > 0.0 || (value.high == 0.0 && value.low > 0.0)

        override fun negate(value: DoubleDouble): DoubleDouble =
            DoubleDouble(-value.high, -value.low)
        override fun absoluteValue(value: DoubleDouble): DoubleDouble =
            DoubleDouble(value.high.absoluteValue, value.low.absoluteValue)
    }

    object Addition : AbstractDoubleFloatingPointAddition<Double, DoubleDouble>(TypeTraits.Double, TypeTraits.Double, Signed) {
        override val comparator
            get() = naturalOrder<Double>()

        override fun create(high: Double, low: Double) = DoubleDouble(high, low)
    }

    object Multiplication : AbstractDoubleFloatingPointMultiplication<Double, DoubleDouble>(TypeTraits.Double, TypeTraits.Double) {
        override val addition
            get() = Addition

        override fun create(high: Double, low: Double): DoubleDouble = DoubleDouble(high, low)

        // 2^ceil(Double.PRECISION/2) + 1 == 2^27 + 1
        override val splitPoint: Double = DoubleBits(0x41A0000002000000).toFloatingPoint()
    }

    object Division : DoubleFloatingPointDivision<Double, DoubleDouble> by DoubleFloatingPointLongDivision(
        baseTraits = TypeTraits.Double,
        baseSigned = TypeTraits.Double,
        baseArithmetic = TypeTraits.Double,
        addition = Addition,
        multiplication = Multiplication
    )
    // TODO Find a way to support FMA on JVM

    override fun toFloatingPoint(): Double = high + low

    operator fun unaryMinus(): DoubleDouble = Signed.negate(this)
    operator fun plus(rhs: Double): DoubleDouble = Addition.twoSum(this, rhs)
    operator fun plus(rhs: DoubleDouble): DoubleDouble = Addition.twoSum(this, rhs)
    operator fun minus(rhs: Double): DoubleDouble = Addition.twoSum(this, -rhs)
    operator fun minus(rhs: DoubleDouble): DoubleDouble = Addition.twoSum(this, -rhs)
    operator fun times(rhs: Double): DoubleDouble = Multiplication.twoProduct(this, rhs)
    operator fun times(rhs: DoubleDouble): DoubleDouble = Multiplication.twoProduct(this, rhs)
    operator fun div(rhs: Double): DoubleDouble = Division.divide(this, rhs)
    operator fun div(rhs: DoubleDouble): DoubleDouble = Division.divide(this, rhs)
}
