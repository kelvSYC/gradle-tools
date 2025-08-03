package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to decimal floating-point types.
 *
 * @param T The floating-point type
 */
interface DecimalFloatingPointTraits<T> {
    /**
     * The number of bits in the continuation field of the decimal floating-point value.
     */
    val continuationWidth: Int

    /**
     * The number of bits in the combination field of the decimal floating-point value.
     */
    val combinationWidth: Int

    /**
     * The number of decimal digits represented in the significand of the decimal floating-point value.
     */
    val precision: Int

    /**
     * The number of bits in the exponent of the decimal floating-point value.
     */
    val exponentBits: Int

    /**
     * The number of bits in the significand of the decimal floating-point value.
     */
    val significandBits: Int

    /**
     * The exponent bias of the decimal floating-point value. When subtracted from the biased exponent, the decimal
     * point will be located after the first significant digit.
     */
    val exponentBias: Int

    /**
     * The exponent bias of the decimal floating-point value, if the significand is to be taken as an integral value.
     */
    val integralExponentBias: Int

    /**
     * The range of valid exponents of a decimal floating-point value, if the decimal point is located after the first
     * significant digit.
     */
    val exponentRange: IntRange

    /**
     * The range of valid exponents of a decimal floating-point value, if the significand is to be taken as an integral
     * value.
     */
    val integralExponentRange: IntRange

    /**
     * The value representing the normalized representation of a (positive) zero value.
     */
    val zero: T

    /**
     * The value representing the canonical representation of a positive infinite value.
     */
    val positiveInfinity: T

    /**
     * The value representing the canonical representation of a negative infinite value.
     */
    val negativeInfinity: T

    /**
     * The value representing the canonical representation of a quiet NaN value.
     */
    val quietNaN: T
}
