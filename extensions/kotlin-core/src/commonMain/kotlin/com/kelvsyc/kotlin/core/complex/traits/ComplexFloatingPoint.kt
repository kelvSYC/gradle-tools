package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex

interface ComplexFloatingPoint<T, C : Complex<T>> {
    val zero: C

    val one: C

    val i: C

    /**
     * Returns `true` if the value represents an infinite value.
     *
     * To be consistent with the definition of complex floating-point types in C, a value is considered infinite if
     * either the real or imaginary parts are infinite, irrespective of the value of the other part.
     */
    fun isInfinite(value: C): Boolean

    /**
     * Returns `true` if the value represents a `NaN`.
     *
     * To be consistent with the definition of complex floating-point types in C, a value is considered a NaN if
     * either the real or imaginary part is a NaN, and the other part is not infinite.
     */
    fun isNaN(value: C): Boolean

    /**
     * Returns `true` if the value is a finite value.
     *
     * To be consistent with the definition of complex floating-point types in C, a value is considered finite if both
     * the real and imaginary parts are neither infinite or NaN.
     */
    fun isFinite(value: C): Boolean
}
