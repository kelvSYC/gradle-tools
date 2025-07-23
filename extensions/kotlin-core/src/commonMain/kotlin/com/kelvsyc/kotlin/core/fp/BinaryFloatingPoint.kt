package com.kelvsyc.kotlin.core.fp

/**
 * Abstract representation of a binary floating-point value. This type is mainly used to facilitate conversions, rather
 * than to perform floating-point operations on.
 *
 * The representation of a value is not guaranteed to be unique. Because of this, there is no correlation between, for
 * example, a [BinaryFloatingPoint]<[Int]> and a [Float].
 *
 * @param B A suitable backing integral type for the significand.
 */
// FIXME B should be an unsigned type
sealed interface BinaryFloatingPoint<B> {
    /**
     * Abstract representation of a finite binary floating-point value.
     *
     * @param signBit The sign of the value. `true` if the value is negative.
     * @param exponent The binary exponent of the value, if the significand is treated as an integral value.
     * @param significand The significand of the value, treated as an integer.
     * @param B A suitable backing integral type for the significand.
     */
    data class Finite<B>(val signBit: Boolean, val exponent: Int, val significand: B) : BinaryFloatingPoint<B>

    /**
     * Abstract representation of an infinite binary floating-point value.
     *
     * @param signBit The sign of the value. `true` if the value is negative.
     * @param B A suitable backing integral type for the significand.
     */
    data class Infinity<B>(val signBit: Boolean) : BinaryFloatingPoint<B>

    /**
     * Abstract representation of a NaN.
     *
     * @param payload An implementation-defined payload.
     * @param B A suitable backing integral type for holding the payload.
     */
    // TODO require nonzero payload
    data class NaN<B>(val payload: B) : BinaryFloatingPoint<B>
}
