package com.kelvsyc.kotlin.core.traits

/**
 * Interface representing basic bit shifting operations on a type.
 *
 * Note that this interface does not impose the standard infix operations (`shl`, `shr`, etc.) on the type. This is
 * because types may have inherent infix operators or that types may have multiple implementations of this.
 *
 * @param T The type supporting bit shifting operations.
 */
interface BitShift<T> {
    /**
     * Shifts the value left by the specified number of bits.
     *
     * The behavior of a negative shift, or a shift in excess of the size of the value type, is undefined.
     */
    fun leftShift(value: T, bitCount: Int): T

    /**
     * Shifts the value right by the specified number of bits.
     *
     * The behavior of a negative shift, or a shift in excess of the size of the value type, is undefined.
     */
    fun rightShift(value: T, bitCount: Int): T

    /**
     * Performs an arithmetic right shift of the specified value, shifting the value right by the specified number of
     * bits. The bit shifted in will be the value of the most significant bit of the original value.
     *
     * The behavior of a negative shift, or a shift in excess of the size of the value type, is undefined.
     */
    fun arithmeticRightShift(value: T, bitCount: Int): T
}
