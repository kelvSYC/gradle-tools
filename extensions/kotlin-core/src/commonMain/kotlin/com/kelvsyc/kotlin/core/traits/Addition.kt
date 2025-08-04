package com.kelvsyc.kotlin.core.traits

/**
 * Interface representing the addition and subtraction operations on a type.
 *
 * Note that this interface does not impose the standard infix operators (`plus`, `minus`, etc.) on the type. This is
 * because types may have inherent infix operators or that types may have multiple implementations of this.
 *
 * @param T The type supporting addition and subtraction operations.
 */
interface Addition<T> {
    /**
     * Adds the two operands together.
     *
     * Note that this operation may overflow, in which case behavior may be undefined.
     */
    fun add(lhs: T, rhs: T): T

    /**
     * Subtracts the two operands together.
     *
     * Note that this operation may underflow, in which case behavior may be undefined.
     */
    fun subtract(lhs: T, rhs: T): T
}
