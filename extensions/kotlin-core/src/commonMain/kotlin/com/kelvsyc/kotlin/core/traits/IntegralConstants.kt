package com.kelvsyc.kotlin.core.traits

/**
 * Traits interface providing constants for a particular integral type.
 */
interface IntegralConstants<T> {
    /**
     * The zero for the specified type.
     */
    val zero: T

    /**
     * Determines if the specified value is zero.
     */
    fun isZero(value: T): Boolean

    /**
     * The smallest value representable by the specific type.
     */
    val minValue: T

    /**
     * The largest value representable by the specific type.
     */
    val maxValue: T
}
