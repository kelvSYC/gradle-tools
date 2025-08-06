package com.kelvsyc.kotlin.core.traits

/**
 * Traits interface denoting that an integral type supports the greatest common divisor operation.
 */
interface GreatestCommonDivisor<T> {
    /**
     * Computes the greatest common divisor between the two operands.
     *
     * Implementations are not required to support the case when one or both operands are negative.
     */
    fun gcd(lhs: T, rhs: T): T
}
