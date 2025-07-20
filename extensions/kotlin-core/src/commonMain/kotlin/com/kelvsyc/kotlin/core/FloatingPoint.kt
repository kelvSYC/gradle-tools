package com.kelvsyc.kotlin.core

/**
 * Interface denoting that a specific type is a floating-point type.
 */
interface FloatingPoint<T> {
    val zero: T

    val one: T

    fun isNaN(value: T): Boolean

    fun isFinite(value: T): Boolean

    fun isInfinite(value: T): Boolean
}
