package com.kelvsyc.kotlin.core.traits

/**
 * Interface representing bit rotation operations on a type.
 *
 * @param T The type supporting bit rotation operations.
 */
interface BitRotate<T> {
    /**
     * Performs a left rotation of this value, by the specified number of bits.
     *
     * If a negative rotation is specified, a right rotation will be performed instead. Rotating by a value in excess of
     * the size of the value type will result in the rotation of the value by that value, modulo the size of the value
     * type.
     */
    fun rotateLeft(value: T, bitCount: Int): T

    /**
     * Performs a right rotation of this value, by the specified number of bits.
     *
     * If a negative roation is specified, a left rotation will be performed instead. Rotating by a value in excess of
     * the size of the value type will result in the rotation of the value by that value, modulo the size of the value
     * type.
     */
    fun rotateRight(value: T, bitCount: Int): T
}
