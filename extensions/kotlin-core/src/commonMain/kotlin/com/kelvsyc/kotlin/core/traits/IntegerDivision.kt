package com.kelvsyc.kotlin.core.traits

/**
 * Trait interface denoting that a type supports operations relating to integer division.
 */
interface IntegerDivision<T> : Division<T> {
    /**
     * Divides the left-hand value by the right-hand value, rounding the result to the integer closer to negative
     * infinity.
     */
    fun floorDiv(lhs: T, rhs: T): T

    /**
     * Divides the left-hand value by the right-hand value, rounding the result to the integer closer to positive
     * infinity.
     */
    fun ceilDiv(lhs: T, rhs: T): T

    /**
     * Returns the remainder from dividing the left-hand value by the right-hand value, through the use of "truncating
     * division".
     *
     * If the type is [Signed], the sign of the returned value, if nonzero, will be the same as that of the left-hand
     * value. The absolute value of the result will be smaller than that of the right-hand-side.
     */
    fun rem(lhs: T, rhs: T): T

    /**
     * Returns the remainder from dividing the left-hand value by the right-hand value, through the use of "flooring
     * division".
     *
     * If the type is [Signed], the sign of the returned value, if nonzero, will be the same as that of the right-hand
     * value. The absolute value of the result will be smaller than that of the right-hand-side.
     */
    fun mod(lhs: T, rhs: T): T
}
