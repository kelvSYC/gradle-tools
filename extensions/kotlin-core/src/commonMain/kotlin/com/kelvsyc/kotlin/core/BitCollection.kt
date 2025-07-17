package com.kelvsyc.kotlin.core

/**
 * Interface representing operations on a type with respect to it being a collection of bits.
 *
 * @param T The type supporting bit collection operations
 */
interface BitCollection<T> {
    /**
     * Returns the value as a sequence of bits, starting with the least significant bit
     */
    fun asBitSequence(value: T): Sequence<Boolean>

    /**
     * Returns the value as a collection of integers denoting the bits that are set, with `0` being the least
     * significant bit.
     */
    fun getSetBits(value: T): Set<Int>

    fun countLeadingZeroBits(value: T): Int

    fun countTrailingZeroBits(value: T): Int
}
