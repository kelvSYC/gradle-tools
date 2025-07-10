package com.kelvsyc.kotlin.core

/**
 * Interface representing the bitwise operations on a type.
 *
 * Note that this interface does not impose the standard infix operators (`and`, `or`, etc.) on the type. This is
 * because types may have inherent infix operators or that types may have multiple implementations of this.
 *
 * @param T The type supporting bitwise operations.
 */
interface Bitwise<T> {
    /**
     * Performs a bitwise AND of the two operands.
     */
    fun and(lhs: T, rhs: T): T

    /**
     * Performs a bitwise OR of the two operands.
     */
    fun or(lhs: T, rhs: T): T

    /**
     * Performs a bitwise XOR of the two operands.
     */
    fun xor(lhs: T, rhs: T): T

    /**
     * Performs a bit inversion of the value.
     */
    fun inv(value: T): T
}
