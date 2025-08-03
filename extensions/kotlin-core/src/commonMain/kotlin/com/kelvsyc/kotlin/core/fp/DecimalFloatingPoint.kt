package com.kelvsyc.kotlin.core.fp

/**
 * Abstract representation of a decimal floating-point value. This type is mainly used to facilitate conversions, rather
 * than to perform floating-point operations on.
 *
 * The representation of a value is not guaranteed to be unique, as decimal floating-point representations are not
 * guaranteed to be unique.
 *
 * @param B A suitable backing integral type for the significand.
 */
// FIXME B should be an unsigned type
sealed interface DecimalFloatingPoint<B> {
    /**
     * Abstract representation of a finite decimal floating-point value.
     *
     * @param signBit The sign of the value. `true` if the value is negative.
     * @param exponent The decimal exponent of the value, if the significand is treated as an integral value.
     * @param significand The significand of the value, treated as an integer.
     * @param B A suitable backing integral type for the significand.
     */
    data class Finite<B>(val signBit: Boolean, val exponent: Int, val significand: B): DecimalFloatingPoint<B>

    /**
     * Abstract representation of an infinite decimal floating-point value.
     *
     * @param signBit The sign of the value. `true` if the value is negative.
     * @param B A suitable backing integral type for the significand.
     */
    data class Infinity<B>(val signBit: Boolean) : DecimalFloatingPoint<B>

    /**
     * Abstract representation of a NaN.
     *
     * @param signalling `true` if the NaN represents a signalling NaN.
     * @param payload An implementation-defined payload.
     * @param B A suitable backing integral type for holding the payload.
     */
    data class NaN<B>(val signalling: Boolean, val payload: B) : DecimalFloatingPoint<B>
}

