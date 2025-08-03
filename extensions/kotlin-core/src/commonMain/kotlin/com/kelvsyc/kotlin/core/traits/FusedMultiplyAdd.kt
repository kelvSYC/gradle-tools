package com.kelvsyc.kotlin.core.traits

/**
 * Interface denoting that a floating-point type supports the "fused multiply add" operation. The "fused multiply add"
 * operation returns the exact product of the first two arguments, added with the third argument, rounded to the nearest
 * suitable floating point value, avoiding the rounding errors accured from separate multiplication and addition.
 */
interface FusedMultiplyAdd<T> {
    /**
     * Returns the fused multiply add of its three operands. The first two operands will be multiplied, and the third
     * operand will be added to its result.
     *
     * * If any of the arguments is a NaN, the result will be a NaN.
     * * If either of the first two arguments is infinite and the other is zero, the result will be a NaN.
     * * If either of the first two arguments is infinite, and the third argument is an infinite of the opposite sign,
     *   the result will be a NaN.
     */
    fun fma(a: T, b: T, c: T): T
}
