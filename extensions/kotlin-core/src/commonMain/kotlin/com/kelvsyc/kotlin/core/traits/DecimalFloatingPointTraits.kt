package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to decimal floating-point types.
 *
 * @param T The floating-point type
 */
interface DecimalFloatingPointTraits<T> {
    /**
     * The number of bits in the decimal floating-point value
     */
    val sizeBits: Int

    /**
     * The number of bits in the continuation field of the decimal floating-point value.
     */
    val continuationWidth: Int

    /**
     * The number of bits in the combination field of the decimal floating-point value.
     */
    val combinationWidth: Int
        get() = sizeBits - continuationWidth

    /**
     * The number of decimal digits represented in the significand of the decimal floating-point value.
     */
    val precision: Int
        get() = (continuationWidth / 10) * 3 + 1

    /**
     * The number of bits in the exponent of the decimal floating-point value.
     */
    val exponentBits: Int
        get() = combinationWidth - 3

    /**
     * The number of bits in the significand of the decimal floating-point value.
     */
    val significandBits: Int
        get() = continuationWidth + 4

    /**
     * The exponent bias of the decimal floating-point value. When subtracted from the biased exponent, the decimal
     * point will be located after the first significant digit.
     */
    val exponentBias: Int
        get() = (1 shl (exponentBits - 2)) * 3

    /**
     * The exponent bias of the decimal floating-point value, if the significand is to be taken as an integral value.
     */
    val integralExponentBias: Int
        get() = exponentBias + precision

    /**
     * The range of valid exponents of a decimal floating-point value, if the decimal point is located after the first
     * significant digit.
     */
    val exponentRange: IntRange
        get() = (1 - exponentBias) .. exponentBias

    /**
     * The range of valid exponents of a decimal floating-point value, if the significand is to be taken as an integral
     * value.
     */
    val integralExponentRange: IntRange
        get() = (1 - exponentBias - precision) ..  (exponentBias - precision)

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
