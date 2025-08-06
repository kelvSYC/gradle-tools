package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.Multiplication

/**
 * Trait interface denoting multiplication operations on a [Complex] and [Imaginary] type, each backed by a
 * floating-point type.
 *
 * Note that this trait does not extend [Multiplication]. It is intended that implementations implement the
 * multiplication of two real values through delegation to a [Multiplication] implementation.
 */
interface ComplexMultiplication<T, I : Imaginary<T>, C : Complex<T>> :
    ImaginaryMultiplication<T, I>,
    ComplexOnlyMultiplication<T, C> {
    fun multiply(lhs: I, rhs: C): C
    fun multiply(lhs: C, rhs: I): C
}
