package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.FloatingPoint
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.math.absoluteValue
import com.kelvsyc.kotlin.core.Signed as BaseSigned

/**
 * `DoubleFloat` is a "double word" number, extending [Float] by using a second non-overlapping [Float] to extend
 * the precision of the first, though not to the extent of the precision offered by [Double].
 */
class DoubleFloat private constructor(
    override val high: Float, override val low: Float
) : DoubleFloatingPoint<Float> {
    companion object {
        fun of(value: Float) = DoubleFloat(value, 0.0f)
    }

    object Signed : DoubleFloatingPoint.Signed<Float, DoubleFloat> {
        override val base: BaseSigned<Float>
            get() = TypeTraits.Float

        override fun isNegative(value: DoubleFloat): Boolean =
            value.high < 0.0f || (value.high == 0.0f && value.low < 0.0f)

        override fun isPositive(value: DoubleFloat): Boolean =
            value.high > 0.0f || (value.high == 0.0f && value.low > 0.0f)

        override fun negate(value: DoubleFloat): DoubleFloat =
            DoubleFloat(-value.high, -value.low)
        override fun absoluteValue(value: DoubleFloat): DoubleFloat =
            DoubleFloat(value.high.absoluteValue, value.low.absoluteValue)
    }

    object Addition : AbstractDoubleFloatingPointAddition<Float, DoubleFloat>() {
        override val base
            get() = TypeTraits.Float
        override val traits: FloatingPoint<Float>
            get() = TypeTraits.Float
        override val signed
            get() = Signed
        override val comparator
            get() = naturalOrder<Float>()

        override fun create(high: Float, low: Float): DoubleFloat = DoubleFloat(high, low)
    }

    override fun toFloatingPoint(): Float = high + low

    operator fun unaryMinus(): DoubleFloat = Signed.negate(this)
    operator fun plus(rhs: Float) = Addition.twoSum(this, rhs)
    operator fun plus(rhs: DoubleFloat) = Addition.twoSum(this, rhs)
    operator fun minus(rhs: Float) = Addition.twoSum(this, -rhs)
    operator fun minus(rhs: DoubleFloat) = Addition.twoSum(this, -rhs)
}
