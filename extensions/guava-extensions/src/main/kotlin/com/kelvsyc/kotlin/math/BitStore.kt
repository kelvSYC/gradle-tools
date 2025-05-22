package com.kelvsyc.kotlin.math

/**
 * Interface representing a collection of bits.
 *
 * This type is generally implemented by value classes.
 *
 * @param S self-type
 * @param R the backing data store type
 */
@Suppress("detekt:TooManyFunctions")
interface BitStore<S : BitStore<S, R>, R> {
    /**
     * Interface representing metadata relating to a [BitStore].
     *
     * This type is generally implemented by companion objects of [BitStore] subclasses.
     *
     * @param S The type of the [BitStore]
     */
    interface AbstractCompanion<S : BitStore<S, R>, R> {
        /**
         * Returns the number of bits in instances of the type [S].
         */
        val sizeBits: Int

        /**
         * Creates a bit store with the specified raw representation.
         */
        fun create(bits: R): S

        /**
         * Creates a bit store with the specified bits set. The bits specified must be in the range `0` to
         * `sizeBits - 1`, inclusive; any bits outside that range are ignored.
         */
        fun create(bits: Iterable<Int> = emptySet()): S
    }

    /**
     * Return the raw representation of this bit store.
     */
    val bits: R

    infix fun and(other: S): S
    infix fun or(other: S): S
    infix fun xor(other: S): S
    fun inv(): S

    infix fun shl(bitCount: Int): S
    infix fun shr(bitCount: Int): S
    infix fun ushr(bitCount: Int): S

    operator fun get(position: Int): Boolean

    /**
     * Return this bit store as a set of integers representing the bits that are set.
     */
    fun asSet(): Set<Int>

    val trailingZeroes: Int
}
