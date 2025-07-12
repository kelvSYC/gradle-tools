package com.kelvsyc.kotlin.core

import kotlin.reflect.KClass
import com.kelvsyc.kotlin.core.Arithmetic as BaseArithmetic

/**
 * Implementation of a "double-double", a representation of a higher-precision floating-point value backed by two
 * [Float] instances - one larger primary ([value]) and one smaller ([error]) extending the precision of the primary
 * value. The number represented is the sum of the two values.
 */
class DoubleFloat private constructor(value: Float, error: Float) : AbstractDoubleFloatingPoint<DoubleFloat, Float>(value, error) {
    companion object : AbstractCompanion<DoubleFloat, Float>() {
        override fun create(value: Float, error: Float): DoubleFloat = DoubleFloat(value, error) // FIXME normalize using two-sum

        override val underlyingType: KClass<Float> = Float::class
        override val doubledType: KClass<DoubleFloat> = DoubleFloat::class

        override val zero: Float = 0.0f
        override val precision: Int = java.lang.Float.PRECISION
        override val splitter: Float = (1 shl splitPoint).toFloat() + 1.0f

        override val baseComparator: Comparator<Float> = Comparator.naturalOrder()

        override val fma: ((Float, Float, Float) -> Float) = Math::fma
    }

    override val arithmetic: BaseArithmetic<Float> = BaseArithmetic.FloatArithmetic
    override val traits: AbstractCompanion<DoubleFloat, Float> = Companion

    /**
     * Converts this floating-point value to a [Float], by discarding the [error].
     */
    fun toFloat() = value

    /**
     * Converts this floating-point value to a [Double], by summing the [value] and [error].
     */
    fun toDouble() = value.toDouble() + error.toDouble()

    /**
     * Converts this floating-point value to an [Int] representation, where the upper half of the bits represent the
     * value and the lower half of the bits represent the error.
     */
    fun toIntBits() = (value.toRawBits() shl Float.SIZE_BITS) or error.toRawBits()
}
