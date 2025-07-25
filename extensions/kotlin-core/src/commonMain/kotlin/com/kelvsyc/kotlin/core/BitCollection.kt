package com.kelvsyc.kotlin.core

/**
 * Interface representing operations on a type with respect to it being a collection of bits.
 *
 * @param T The type supporting bit collection operations
 */
interface BitCollection<T> {
    /**
     * The size of the bit collection.
     */
    val sizeBits: Int

    /**
     * Creates a bit collection whose set bits are specified by the supplied [IntRange].
     */
    fun fromBits(bits: IntRange): T

    /**
     * Returns the value as a sequence of bits, starting with the least significant bit
     */
    fun asBitSequence(value: T): Sequence<Boolean>

    /**
     * Returns the bit collection as an array of bytes, with the least significant byte first.
     */
    fun asByteArray(value: T): ByteArray

    /**
     * Returns the value as a collection of integers denoting the bits that are set, with `0` being the least
     * significant bit.
     */
    fun getSetBits(value: T): Set<Int>

    /**
     * Returns whether or not the value is zero - that is, `true` if no bits are set.
     */
    fun isZero(value: T): Boolean

    /**
     * Returns the number of bits, starting with the most significant bit, that are not set, before finding the first
     * set bit.
     *
     * This function returns the size of the bit collection if the value is zero.
     */
    fun countLeadingZeroBits(value: T): Int

    /**
     * Returns the number of bits, starting with the least significant bit, that are not set, before finding the first
     * set bit.
     *
     * This function returns the size of the bit collection if the value is zero.
     */
    fun countTrailingZeroBits(value: T): Int
}
