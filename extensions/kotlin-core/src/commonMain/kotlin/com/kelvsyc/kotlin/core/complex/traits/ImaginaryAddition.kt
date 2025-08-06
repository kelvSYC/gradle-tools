package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary

/**
 * Trait interface denoting addition and subtraction operations on an [Imaginary] type, backed by a floating-point type.
 */
interface ImaginaryAddition<T, I : Imaginary<T>> {
    fun add(lhs: I, rhs: I): I

    fun subtract(lhs: I, rhs: I): I
}
