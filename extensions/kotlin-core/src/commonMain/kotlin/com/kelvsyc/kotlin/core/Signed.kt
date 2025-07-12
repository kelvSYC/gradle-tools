package com.kelvsyc.kotlin.core

/**
 * Interface denoting that the value type is a signed value type.
 *
 * Note that this interface does not impose the standard infix operator (`unaryMinus`) on the type. This is
 * because types may have an inherent operation or that types may have multiple implementations of this.
 *
 * @param T The type supporting the negation operation.
 */
interface Signed<T> {
    /**
     * Returns `true` if the supplied value is a positive value.
     *
     * A type with an unsigned zero must return `false` for this function and [isNegative] when zero is supplied.
     */
    fun isPositive(value: T): Boolean

    /**
     * Returns `true` if the supplied value is a negative value.
     *
     * A type with an unsigned zero must return `false` for this function and [isPositive] when zero is supplied.
     */
    fun isNegative(value: T): Boolean

    /**
     * Returns a value of the same magnitude but of the opposite sign.
     *
     * Note that a zero may be signed or unsigned. If a zero is unsigned, this function will return its input if
     * a zero is supplied.
     */
    fun negate(value: T): T
}
