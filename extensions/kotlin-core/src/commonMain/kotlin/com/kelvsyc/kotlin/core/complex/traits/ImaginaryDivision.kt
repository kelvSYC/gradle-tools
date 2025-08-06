package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary

/**
 * Trait interface denoting division operations on an [Imaginary] type, backed by a floating-point type.
 */
interface ImaginaryDivision<T, I : Imaginary<T>> {
    fun divide(lhs: I, rhs: I): T

    fun divide(lhs: T, rhs: I): I
    fun divide(lhs: I, rhs: T): I
}
