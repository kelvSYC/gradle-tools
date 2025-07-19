package com.kelvsyc.kotlin.core

/**
 * Interface denoting that a type supports arithmetic operations.
 *
 * Note that this interface does not impose the standard operator overloads (eg. `plus`, `minus`) on the type.
 *
 * @param T The type supporting bitwise operations.
 */
interface Arithmetic<T> {
    fun add(lhs: T, rhs: T): T
    fun subtract(lhs: T, rhs: T): T
    fun multiply(lhs: T, rhs: T): T
    fun divide(lhs: T, rhs: T): T

    fun negate(value: T): T
}
