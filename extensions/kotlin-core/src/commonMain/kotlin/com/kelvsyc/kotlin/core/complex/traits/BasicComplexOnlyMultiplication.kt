package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ComplexOnlyMultiplication] for a [Complex] type.
 */
class BasicComplexOnlyMultiplication<T, C : Complex<T>>(
    private val arithmetic: FloatingPointArithmetic<T>,
    private val factory: Complex.Factory<T, C>
) : ComplexOnlyMultiplication<T, C> {
    override fun multiply(lhs: T, rhs: T): T = arithmetic.multiply(lhs, rhs)
    override fun multiply(lhs: C, rhs: C): C = factory.ofCartesian(
        arithmetic.subtract(arithmetic.multiply(lhs.real, rhs.real), arithmetic.multiply(lhs.imaginary, rhs.imaginary)),
        arithmetic.add(arithmetic.multiply(lhs.real, rhs.imaginary), arithmetic.multiply(lhs.imaginary, rhs.real))
    )

    override fun multiply(lhs: T, rhs: C): C =
        factory.ofCartesian(arithmetic.multiply(lhs, rhs.real), arithmetic.multiply(lhs, rhs.imaginary))
    override fun multiply(lhs: C, rhs: T): C =
        factory.ofCartesian(arithmetic.multiply(lhs.real, rhs), arithmetic.multiply(lhs.imaginary, rhs))
}
