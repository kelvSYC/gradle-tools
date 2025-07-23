package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.fp.BinaryFloatingPoint

/**
 * Interface representing down the bit representation of a floating-point value.
 *
 * @param T The floating-point value
 * @param B A bit store type the same size as the floating-point value
 */
interface FloatingPointBits<T, B> {
    /**
     * Retrieves the value of the sign bit.
     *
     * A `true` value denotes that the value is negative.
     */
    val signBit: Boolean

    /**
     * Retrieves the value of the biased exponent.
     *
     * Standard floating-point values give special meaning to a biased exponent of 0 and a biased exponent representing
     * the largest possible value therein.
     */
    val biasedExponent: Int

    /**
     * Returns the value of the mantissa or significand, without its implicit leading bit.
     *
     * The mantissa generally represents the fractional part of a number known as a "significand", whose integral part,
     * sometimes known as the "leading bit", is implicit based on the [biasedExponent].
     */
    val mantissa: B

    /**
     * Retrieves the unbiased exponent value.
     *
     * This value is obtained from subtracting the inherent exponent bias from [biasedExponent]. As such, the two biased
     * exponents with special meaning (0 and the largest value) will be represented by values that are one smaller than
     * the smallest unbiased exponent or one larger than the largest unbiased exponent.
     */
    val exponent: Int

    /**
     * Retrieves the unbiased exponent value.
     *
     * This value is obtained from subtracting the integral exponent bias from [biasedExponent]. As such, the two biased
     * exponents with special meaning (0 and the largest value) will be represented by values that are one smaller than
     * the smallest unbiased integral exponent or one larger than the largest unbiased integral exponent.
     */
    val integralExponent: Int

    /**
     * Retrieves the significand of the floating-point value.
     *
     * For [normal][isNormal] floating point values, the significand consists of a "leading bit" representing the
     * integral part, as well as the [mantissa] representing the fractional part. For [subnormal][isSubnormal] values,
     * the significand is the [mantissa] shifted over by one bit, as it is interpreted as if its [biasedExponent] is
     * taken literally. For all other values, the [mantissa] is returned directly.
     */
    val significand: B

    /**
     * Returns `true` if the number represents a normal floating-point value.
     *
     * A normal floating-point value contains a [biasedExponent] that is not at its smallest (`0`) or largest possible
     * values. The [mantissa] represents the fractional part of the number, alongside an implicit `1` in front of the
     * "binary point".
     */
    val isNormal: Boolean

    /**
     * Returns `true` if the number represents a zero.
     *
     * A floating-point type is zero if both its [biasedExponent] and its [mantissa] is zero. Note that as a result,
     * there is both a positive and negative zero.
     */
    val isZero: Boolean

    /**
     * Returns `true` if the number represents a subnormal floating-point value.
     *
     * A subnormal floating-point value contains a [biasedExponent] that is zero, and a non-zero [mantissa]. The
     * [mantissa] represents the fractional part of the number, alongside an implicit `0` in front of the "binary
     * point".
     */
    val isSubnormal: Boolean

    /**
     * Returns `true` if the number represents an infinite value.
     *
     * An infinite floating-point value contains a [biasedExponent] that is at its maximum value, and a zero [mantissa].
     */
    val isInfinite: Boolean

    /**
     * Returns `true` if the number represents a NaN.
     *
     * A NaN, or "not a number", contains a [biasedExponent] that is at its maximum value, and a non-zero [mantissa].
     * NaN values are further subdivided into "quiet NaN" and "signalling NaN"; the distinction between these is
     * implementation-dependent.
     */
    val isNaN: Boolean

    /**
     * Returns `true` if the number represents a finite value.
     *
     * A floating-point value is finite if its [biasedExponent] is not at its maximum value. That is, if both [isInfinite]
     * and [isNaN] are false.
     */
    val isFinite: Boolean
        get() = !isInfinite && !isNaN

    /**
     * Returns `true` if the number represents a value that can be represented exactly by an integral type of sufficient
     * size.
     */
    val isMathematicalInteger: Boolean

    /**
     * Returns `true` if the number represents a power of 2.
     */
    val isPowerOfTwo: Boolean

    /**
     * Converts this bit representation to an instance of the floating-point type.
     */
    fun toFloatingPoint(): T

    /**
     * Converts this bit representation to an instance of [BinaryFloatingPoint].
     */
    fun toBinaryFloatingPoint(): BinaryFloatingPoint<B> = if (isInfinite) {
        BinaryFloatingPoint.Infinity(signBit)
    } else if (isNaN) {
        BinaryFloatingPoint.NaN(mantissa)
    } else {
        BinaryFloatingPoint.Finite(signBit, integralExponent, significand)
    }
}
