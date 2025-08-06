package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary

/**
 * Trait interface denoting multiplication operations on an [Imaginary] type, backed by a floating-point type.
 */
interface ImaginaryMultiplication<T, I : Imaginary<T>> {
    fun multiply(lhs: I, rhs: I): T

    fun multiply(lhs: T, rhs: I): I
    fun multiply(lhs: I, rhs: T): I
}
