package com.kelvsyc.kotlin.core.traits

/**
 * Trait interface denoting that a type supports operations relating to floating-point division.
 */
interface FloatingPointDivision<T> : Division<T> {
    /**
     * Returns the remainder from dividing the left-hand value by the right-hand value, through the use of "truncating
     * division". The result may or may not be exact; if not, it will be rounded to the nearest representable number.
     *
     * The sign of the returned value, if nonzero, will be the same as that of the left-hand value. The absolute value
     * of the result will be no larger than that of the right-hand-side.
     */
    fun rem(lhs: T, rhs: T): T

    /**
     * Returns the remainder from dividing the left-hand value by the right-hand value, through the use of "flooring
     * division". The result may or may not be exact; if not, it will be rounded to the nearest representable number.
     *
     * The sign of the returned value, if nonzero, will be the same as that of the right-hand value. The absolute value
     * of the result will be no larger than that of the right-hand-side.
     */
    fun mod(lhs: T, rhs: T): T
}
