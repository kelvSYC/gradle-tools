package com.kelvsyc.kotlin.core

import kotlin.reflect.KClass

/**
 * Implementation of a "double-double", a representation of a higher-precision floating-point value backed by two
 * [Double] instances - one larger primary ([value]) and one smaller ([error]) extending the precision of the primary
 * value. The number represented is the sum of these two values.
 */
class DoubleDouble private constructor(value: Double, error: Double) : AbstractDoubleFloatingPoint<DoubleDouble, Double>(value, error) {
    companion object : AbstractCompanion<DoubleDouble, Double>() {
        override fun create(value: Double, error: Double): DoubleDouble = DoubleDouble(value, error) // FIXME normalize using two-sum

        override val underlyingType: KClass<Double> = Double::class
        override val doubledType: KClass<DoubleDouble> = DoubleDouble::class

        override val zero: Double = 0.0
        override val precision: Int = java.lang.Double.PRECISION
        override val splitter: Double = (1 shl splitPoint).toDouble() + 1.0

        override val baseComparator: Comparator<Double> = Comparator.naturalOrder()

        override val fma: ((Double, Double, Double) -> Double) = Math::fma
    }

    override val arithmetic: Arithmetic<Double> = Arithmetic.DoubleArithmetic
    override val traits: AbstractCompanion<DoubleDouble, Double> = Companion

    /**
     * Converts this floating-point value to a [Double], by discarding the [error].
     */
    fun toDouble(): Double = value

    /**
     * Converts this floating-point value to a [Long] representation, where the upper half of the bits represent the
     * value and the lower half of the bits represents the error.
     */
    fun toLongBits() = (value.toRawBits() shl Double.SIZE_BITS) or error.toRawBits()
}
