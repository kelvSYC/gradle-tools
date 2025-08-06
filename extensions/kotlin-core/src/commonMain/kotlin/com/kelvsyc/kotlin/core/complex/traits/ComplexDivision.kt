package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPointDivision

/**
 * Trait interface denoting division operations on a [Complex] and [Imaginary] type, each backed by a floating-point
 * type.
 *
 * Note that this trait does not extend [FloatingPointDivision]. It is intended that implementations implement the
 * division of two real values through delegation to a [FloatingPointDivision] implementation.
 */
interface ComplexDivision<T, I : Imaginary<T>, C : Complex<T>> : ImaginaryDivision<T, I>, ComplexOnlyDivision<T, C> {
    fun divide(lhs: I, rhs: C): C
    fun divide(lhs: C, rhs: I): C
}
