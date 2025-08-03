package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.traits.Binary16Sized
import com.kelvsyc.kotlin.core.traits.AbstractBinary16Traits
import com.kelvsyc.kotlin.core.traits.BitsBased
import com.kelvsyc.kotlin.core.traits.FloatingPointTraits
import com.kelvsyc.kotlin.core.traits.Sized

/**
 * Value representing a 16-bit `binary16` floating-point value.
 *
 * This class performs its operations by widening these values to [Float] and then performing a narrowing conversion in
 * the end. Under certain circumstances, it may cause a loss of precision.
 *
 * @param bits The bits used to represent the number, in the form of a 16-bit integral value (a [Short]).
 */
@JvmInline
value class Float16(val bits: Short): Comparable<Float16> {
    companion object {
        /**
         * [Converter] implementation used to convert values between [Float16] (representing a [Short]) and [Float].
         */
        val converter = Converter.of(java.lang.Float::floatToFloat16, java.lang.Float::float16ToFloat)

        /**
         * [Comparator] implemenation used to compare two [Float16] values by widening both values to [Float] and
         * performing the comparison as [Float] values.
         */
        val comparator = Comparator.comparing<Float16, Float> { converter.reverse(it.bits) }

        /**
         * Constant representing `0.0`.
         */
        val zero = Traits.zero

        private val wrappedUnaryPlus = converter.wrap(Float::unaryPlus)
        private val wrappedUnaryMinus = converter.wrap(Float::unaryMinus)
        private val wrappedPlus = converter.wrap(Float::plus)
        private val wrappedMinus = converter.wrap(Float::minus)
        private val wrappedTimes = converter.wrap(Float::times)
        private val wrappedDiv = converter.wrap(Float::div)
        private val wrappedRem = converter.wrap(Float::rem)
    }

    private object TraitsInternal : AbstractBinary16Traits<Float16>() {
        override val zero: Float16 = Float16(0)
        override val one: Float16 = Float16(0x3C00)
        override val positiveInfinity: Float16 = Float16(0x7C00)
        override val negativeInfinity: Float16 = Float16(0xFC00.toShort())
        override val NaN: Float16 = Float16(0xFCE0.toShort())

        override fun isNaN(value: Float16): Boolean = Float16Bits.ofValue(value).isNaN
        override fun isFinite(value: Float16): Boolean = Float16Bits.ofValue(value).isFinite
        override fun isInfinite(value: Float16): Boolean = Float16Bits.ofValue(value).isInfinite
    }

    @Suppress("detekt:TooManyFunctions")
    object Traits : FloatingPointTraits<Float16> by TraitsInternal,
        Sized<Float16> by Binary16Sized(),
        BitsBased<Float16, Short>,
        Addition<Float16>, Multiplication<Float16>, Signed<Float16> {
        override val converter = Converter.of(Float16::bits, ::Float16)

        override fun add(lhs: Float16, rhs: Float16): Float16 = lhs + rhs
        override fun subtract(lhs: Float16, rhs: Float16): Float16 = lhs - rhs

        override fun multiply(lhs: Float16, rhs: Float16): Float16 = lhs * rhs
        override fun divide(lhs: Float16, rhs: Float16): Float16 = lhs / rhs

        override fun isPositive(value: Float16): Boolean = value > zero
        override fun isNegative(value: Float16): Boolean = value < zero
        override fun negate(value: Float16): Float16 = -value
        override fun absoluteValue(value: Float16): Float16 = if (isPositive(value)) value else -value
    }

    /**
     * Creates a new `WideningHalf` from an actual [Float].
     *
     * Note that this is considered a narrowing operation, and thus the created value is the closest value to the given
     * value given the nearest even rounding mode.
     *
     * @see java.lang.Float.floatToFloat16
     */
    constructor(value: Float) : this(converter(value))

    /**
     * Converts this value to a [Float].
     *
     * This is considered a widening operation.
     */
    fun toFloat(): Float = converter.reverse(bits)

    operator fun unaryPlus(): Float16 = Float16(wrappedUnaryPlus(bits))
    operator fun unaryMinus(): Float16 = Float16(wrappedUnaryMinus(bits))
    operator fun plus(rhs: Float16): Float16 = Float16(wrappedPlus(bits, rhs.bits))
    operator fun minus(rhs: Float16): Float16 = Float16(wrappedMinus(bits, rhs.bits))
    operator fun times(rhs: Float16): Float16 = Float16(wrappedTimes(bits, rhs.bits))
    operator fun div(rhs: Float16): Float16 = Float16(wrappedDiv(bits, rhs.bits))
    operator fun rem(rhs: Float16): Float16 = Float16(wrappedRem(bits, rhs.bits))

    override fun compareTo(other: Float16): Int = comparator.compare(this, other)
}
