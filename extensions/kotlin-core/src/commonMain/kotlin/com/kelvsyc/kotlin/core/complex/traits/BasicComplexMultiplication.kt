package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ComplexMultiplication], where parts are multiplied to each other according to their
 * components.
 */
class BasicComplexMultiplication<T, I : Imaginary<T>, C : Complex<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val imaginary: Imaginary.Factory<T, I>,
    private val complex: Complex.Factory<T, C>
) : ComplexMultiplication<T, I, C>,
    ImaginaryMultiplication<T, I> by BasicImaginaryMultiplication(baseTraits, arithmetic, imaginary),
    ComplexOnlyMultiplication<T, C> by BasicComplexOnlyMultiplication(arithmetic, complex) {
    override fun multiply(lhs: I, rhs: C): C = complex.ofCartesian(
        baseTraits.negate(arithmetic.multiply(lhs.value, rhs.imaginary)),
        arithmetic.multiply(lhs.value, rhs.real)
    )
    override fun multiply(lhs: C, rhs: I): C = complex.ofCartesian(
        baseTraits.negate(arithmetic.multiply(lhs.imaginary, rhs.value)),
        arithmetic.multiply(lhs.real, rhs.value)
    )
}
