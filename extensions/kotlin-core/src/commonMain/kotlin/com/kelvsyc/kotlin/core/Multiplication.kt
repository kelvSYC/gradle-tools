package com.kelvsyc.kotlin.core

/**
 * Interface representing the multiplication operations on a type.
 *
 * Note that this interface does not impose the standard infix operators (`times`, etc.) on the type. This is because
 * types may have inherent infix operators or that types may have conflicting implementations of this.
 *
 * @param T The type supporting multiplication operations.
 */
interface Multiplication<T> {
    /**
     * Multiplies the two operands together.
     *
     * Note that this operation may overflow, in which case behavior may be undefined.
     */
    fun multiply(lhs: T, rhs: T): T
}
