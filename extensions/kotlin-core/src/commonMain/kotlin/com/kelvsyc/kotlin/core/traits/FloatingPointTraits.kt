package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to binary floating-point types.
 *
 * @param T The floating-point type
 */
interface FloatingPointTraits<T> : FloatingPoint<T> {
    /**
     * The number of bits in the mantissa portion of the significand of the binary floating-point value.
     */
    val mantissaWidth: Int

    /**
     * The number of bits in the exponent field of the binary floating-point value.
     */
    val exponentWidth: Int

    /**
     * The total number of bits in the significand of the binary floating-point value.
     */
    val precision: Int

    /**
     * The exponent bias of the binary floating-point value.
     */
    val exponentBias: Int

    /**
     * The exponent bias of the binary floating-point value, if the significand was to be interpreted as an integral
     * value.
     */
    val integralExponentBias: Int

    /**
     * The range of valid exponents of a binary floating-point value.
     */
    val exponentRange: IntRange

    /**
     * The range of valid exponents of a binary floating-point value, if the significand was to be interpreted as an
     * integral value.
     */
    val integralExponentRange: IntRange

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
