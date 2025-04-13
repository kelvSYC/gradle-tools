package com.kelvsyc.kotlin.math

/**
 * Bit store relating to a floating-point type.
 *
 * Note that there is no explicit conversion between the bit store and the floating point type. This is to discourage
 * implementing a floating-point type on top of this store.
 *
 * A floating point type consists of a single sign bit, a number of bits representing a biased exponent, and a number of
 * bits representing the raw significand. The actual significand consists of one more bit of the significand, known
 * as the "hidden bit".
 *
 * @param S the backing [BitStore]
 * @param R the backing [BitStore]'s backing type
 */
interface FloatingPointStore<S : BitStore<S, R>, R> {
    /**
     * Base class representing metadata relating to a [FloatingPointStore],
     *
     * This type is generally implemented by companion objects of [FloatingPointStore] implementations.
     *
     * @param F the type of the [FloatingPointStore]
     * @param S the type of the [FloatingPointStore]'s backing [BitStore]
     * @param R the concrete backing type of the backing [BitStore]
     */
    abstract class AbstractCompanion<F : FloatingPointStore<S, R>, S : BitStore<S, R>, R> {
        protected abstract val traits: BitStore.BitStoreConstants<S, R>

        /**
         * Returns the number of bits in instances of the floating-point type.
         */
        abstract val sizeBits: Int

        /**
         * Returns the number of bits in the significand of the floating-point type.
         */
        abstract val precision: Int

        /**
         * Returns a function converting the raw type to an [Int], used to parse [biasedExponent] from the backing value.
         */
        abstract val rawToInt: (R) -> Int

        /**
         * Returns the number of bits for the exponent of the floating-point type.
         */
        val exponentWidth by lazy { sizeBits - precision }

        /**
         * Returns the exponent bias for the floating-point type
         */
        val exponentBias by lazy { (1 shl (exponentWidth - 1)) - 1 }

        /**
         * Returns the largest unbiased exponent for the floating-point type
         */
        val maxExponent by ::exponentBias

        /**
         * Returns the smallest unbiased exponent for the floating-point type
         */
        val minExponent by lazy { 1 - exponentBias }

        /**
         * Returns an [IntRange] object representing the range of unbiased exponent values for the floating-point type
         */
        val exponentRange by lazy { minExponent ..< maxExponent }

        /**
         * Returns a bit mask representing the part of the floating point type containing the unbiased exponent.
         */
        val exponentMask by lazy { traits.create(precision..< sizeBits - 1) }

        /**
         * Returns a bit mask representing the part of the floating point type containing the raw significand.
         */
        val significandMask by lazy { traits.create(0..< precision - 1) }

        val hiddenBit by lazy { traits.create(setOf(precision - 1)) }

        /**
         * Creates a new store backed by the backing bit store.
         */
        abstract fun create(raw: S): F

        /**
         * Creates a new store backed by the backing raw store.
         */
        fun create(raw: R) = create(traits.create(raw))
    }

    val bits: S

    /**
     * Returns `true` if this value represents a negative floating-point value.
     */
    val isNegative: Boolean

    /**
     * Returns the biased exponent value for this floating-point value.
     */
    val biasedExponent: Int

    /**
     * Returns the raw significand value (without the hidden bit) for this floating-point value.
     */
    val rawSignificand: S

    /**
     * Returns the unbiased exponent value for this floating-point value.
     */
    val exponent: Int

    /**
     * Returns the significand value for this floating-point value.
     */
    val significand: S

    /**
     * Returns `true` if this value represents a positive floating-point value.
     */
    val isPositive: Boolean
        get() = !isNegative

    /**
     * Returns `true` if this value represents a subnormal floating-point value.
     */
    val isSubNormal: Boolean

    /**
     * Returns `true` if this value represents a floating-point zero.
     */
    val isZero: Boolean

    /**
     * Returns `true` if this value represents a finite floating-point value.
     */
    val isFinite: Boolean

    /**
     * Returns `true` if this value represents a normal floating-point value.
     */
    val isNormal: Boolean

    /**
     * Returns `true` if this value represents an infinite floating point value.
     */
    val isInfinity: Boolean

    /**
     * Returns `true` if this value represents a "not-a-number" (NaN) value.
     */
    val isNaN: Boolean

    /**
     * Returns `true` if this value represents a mathematical integer.
     */
    val isMathematicalInteger: Boolean

    /**
     * Returns `true` if this value represents a power of two - `2^k` for some integer `k`.
     */
    val isPowerOfTwo: Boolean
}
