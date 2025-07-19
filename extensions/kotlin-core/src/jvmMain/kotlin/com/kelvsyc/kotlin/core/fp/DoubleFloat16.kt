package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.Float16

/**
 * `DoubleFloat16` is a "double word" number, extending [Float16] by using a second non-overlapping [Float16] to extend
 * the precision of the first, though not to the extent of the precision offered by [Float].
 */
class DoubleFloat16 private constructor(
    override val high: Float16, override val low: Float16
) : DoubleFloatingPoint<Float16> {
    companion object {
        fun of(value: Float16) = DoubleFloat16(value, Float16(0))
    }

    object Signed : AbstractDoubleFloatingPointSigned<Float16, DoubleFloat16>(Float16.Traits) {
        override fun create(high: Float16, low: Float16): DoubleFloat16 = DoubleFloat16(high, low)
    }

    object Addition: AbstractDoubleFloatingPointAddition<Float16, DoubleFloat16>() {
        override val base
            get() = Float16.Traits
        override val traits
            get() = Float16.Traits
        override val signed
            get() = Signed
        override val comparator
            get() = Float16.comparator

        override fun create(high: Float16, low: Float16): DoubleFloat16 = DoubleFloat16(high, low)
    }

    override fun toFloatingPoint(): Float16 = high + low

    operator fun unaryMinus(): DoubleFloat16 = Signed.negate(this)
    operator fun plus(rhs: Float16): DoubleFloat16 = Addition.twoSum(this, rhs)
    operator fun plus(rhs: DoubleFloat16): DoubleFloat16 = Addition.twoSum(this, rhs)
    operator fun minus(rhs: Float16): DoubleFloat16 = Addition.twoSum(this, -rhs)
    operator fun minus(rhs: DoubleFloat16): DoubleFloat16 = Addition.twoSum(this, -rhs)
}
