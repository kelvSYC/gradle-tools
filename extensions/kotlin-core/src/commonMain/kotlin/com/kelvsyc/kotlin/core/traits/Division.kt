package com.kelvsyc.kotlin.core.traits

/**
 * Interface representing the division operation on a type.
 *
 * Note that this interface does not impose the standard infix operators (`div`, etc.) on the type. This is because
 * types may have inherent infix operators or that types may have conflicting implementations of this.
 *
 * @param T The type supporting division operations.
 */
interface Division<T> {
    /**
     * Divides the left operand by the right operand.
     *
     * Note that division by zero is undefined and implementation-dependent.
     */
    fun divide(lhs: T, rhs: T): T
}
