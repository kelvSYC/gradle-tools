package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to binary floating-point types.
 *
 * @param T The floating-point type
 */
interface FloatingPointTraits<T> {
    /**
     * The number of bits in the binary floating-point value.
     */
    val sizeBits: Int

    /**
     * The number of bits in the mantissa portion of the significand of the binary floating-point value.
     */
    val mantissaWidth: Int

    /**
     * The number of bits in the exponent field of the binary floating-point value.
     */
    val exponentWidth: Int
        get() = sizeBits - mantissaWidth - 1

    /**
     * The total number of bits in the significand of the binary floating-point value.
     */
    val precision: Int
        get() = mantissaWidth + 1

    /**
     * The exponent bias of the binary floating-point value.
     */
    val exponentBias: Int
        get() = (1 shl (exponentWidth - 1)) - 1

    /**
     * The exponent bias of the binary floating-point value, if the significand was to be interpreted as an integral
     * value.
     */
    val integralExponentBias: Int
        get() = exponentBias + mantissaWidth

    /**
     * The range of valid exponents of a binary floating-point value.
     */
    val exponentRange: IntRange
        get() = (1 - exponentBias) .. exponentBias

    /**
     * The range of valid exponents of a binary floating-point value, if the significand was to be interpreted as an
     * integral value.
     */
    val integralExponentRange: IntRange
        get() = (1 - exponentBias - mantissaWidth) .. exponentBias - mantissaWidth

    /**
     * The value representing (positive) zero.
     */
    val zero: T

    /**
     * The value representing a positive infinity.
     */
    val positiveInfinity: T

    /**
     * The value representing a negative infinity.
     */
    val negativeInfinity: T

    /**
     * The value representing the cononical representation of a NaN. The value may represent a quiet or signalling NaN.
     */
    val NaN: T
}
