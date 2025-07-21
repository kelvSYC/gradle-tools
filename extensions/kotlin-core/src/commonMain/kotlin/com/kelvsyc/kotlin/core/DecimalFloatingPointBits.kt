package com.kelvsyc.kotlin.core

/**
 * Interface representing the bit representation of a decimal floating-point value.
 *
 * A decimal floating-point value is a floating-point value that uses a decimal, rather than a binary, base. Like their
 * binary counterparts, they consist of a sign bit, exponent, and significand. However, in part due to needing to
 * count for base-10 digits, the representation of a decimal floating-point value is not unique.
 */
interface DecimalFloatingPointBits<T, B> {
    /**
     * Retrieves the value of the sign bit.
     *
     * A `true` value denotes that the value is negative.
     */
    val signBit: Boolean

    /**
     * Retrieves the value of the combination field.
     *
     * The combination field is at least 5 bits, and encodes information about the exponent and the leading bits of
     * the significand. The manner in which the exponent and significand are encoded are based on how the significand
     * itself is encoded.
     */
    val combination: Int

    /**
     * Retrieves the value of the continuation field.
     *
     * The continuation field contains the lower order bits of the significand. The value of the significand is
     * implementation-dependent, though it is guaranteed to be within a decimal range independent of representation.
     */
    val continuation: B

    /**
     * Retrieves the biased exponent of the value.
     *
     * Note that unlike binary floating-point values, decimal floating-point values have unique representations for
     * infinite and `NaN` values, as opposed to having a special value for the biased exponent.
     *
     * This function returns `null` on infinite and NaN values.
     */
    val biasedExponent: Int?

    /**
     * Retrieves the unbiased exponent of the value.
     *
     * Like binary floating-point values, decimal floating-point values are stored with an exponent bias. The returned
     * exponent should be interpreted as if the decimal point occurs after the first gigit of the significand,
     * consistent with its use in binary floating-point values.
     *
     * This function returns `null` on infinite and NaN values.
     */
    val exponent: Int?

    /**
     * Retrieves the unbiased integral exponent of the value.
     *
     * Like binary floating-point values, decimal floating-point values are stored with an exponent bias. The returned
     * exponent should be interpreted as if the significand was to be interpreted as an integral value.
     *
     * This function returns `null` on infinite and NaN values.
     */
    val integralExponent: Int?

    /**
     * Retrieves the significand of the value.
     *
     * Note that the actual numeric value of the significand is implementation-dependent, but it is guaranteed to be
     * within a decimal range independent of representation. This function returns the raw bit representation, using
     * data from both the [combination] and [continuation] fields, possibly including other implicit bits, where
     * applicable.
     *
     * This function returns `null` on infinite and NaN values.
     */
    val significand: B?

    /**
     * Retrieves the significand of the value.
     *
     * Note that the actual numeric value of the significand is implementation-dependent, but it is guaranteed to be
     * within a decimal range independent of representation. This function returns the numeric representation,
     * independent of the underlying significand representation, but as an integral value.
     *
     * This function returns `null` on infinite and NaN values.
     */
    val significandAsNumber: B?

    /**
     * Returns `true` if the number represents a zero.
     *
     * Like a binary floating-point value, a decimal floating-point value is a zero if its [significand] is zero.
     * However, unlike binary floating-point values, the representation of zero is not unique, as the [exponent] may
     * be any value.
     */
    val isZero: Boolean

    /**
     * Returns `true` if the number represents a normal number.
     *
     * A normal number is a number whose significand does not contain a leading zero.
     */
    val isNormal: Boolean

    /**
     * Returns `true` if the number represents a denormal number.
     *
     * A denormal number is a number whose significand contains a leading zero. Note that if the [biasedExponent] is
     * nonzero, there exists a normal number representing the same value.
     */
    val isDenormal: Boolean

    /**
     * Returns `true` if the number represents a subnormal number.
     *
     * A subnormal number is a number whose significand contains a leading zero, and there is no normal number
     * representing the same value - ie. its [biasedExponent] is zero.
     */
    val isSubnormal: Boolean

    /**
     * Returns `true` if the number represents an infinite value.
     *
     * Note that unlike a binary floating-point value, decimal floating-point types have a unique representation of
     * infinite values that do not depend on a special value of a biased exponent. However, the information is encoded
     * in some of the bits of the [combination]. Representations of infinite values are generally consistent across
     * different representations of the significand.
     */
    val isInfinite: Boolean

    /**
     * Returns `true` is the number represents a finite value.
     *
     * Note that any value that is not infinite or a NaN is finite. However, unlike binary floating-point values, due to
     * representations not being unique, they may still be denormal - ie. have a leading zero in the significand if
     * nonzero, or have a nonzero exponent and a zero significand if zero.
     */
    val isFinite: Boolean
        get() = !isInfinite && !isNaN

    /**
     * Returns `true` if the number represents a NaN.
     *
     * A NaN, or "not a number", contains a unique representation among some of the bits of the [combination].
     * Representations of NaN values are generally consistent across different representations of the significand.
     */
    val isNaN: Boolean

    /**
     * Returns `true` if the number represents a quiet NaN.
     *
     * Unlike binary floating-point values, decimal floating-point values have well-defined distinctions between the
     * two main types of NaN values.
     */
    val isQuietNaN: Boolean

    /**
     * Returns `true` if the number represents a signalling NaN.
     *
     * Unlike binary floating-point values, decimal floating-point values have well-defined distinctions between the
     * two main types of NaN values.
     */
    val isSignallingNaN: Boolean

    /**
     * Converts this bit representation to an instance of the decimal floating-point type.
     */
    fun toFloatingPoint(): T
}
