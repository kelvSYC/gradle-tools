package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ComplexAddition], where parts are added to each other component-wise.
 */
class BasicComplexAddition<T, I : Imaginary<T>, C : Complex<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val imaginary: Imaginary.Factory<T, I>,
    private val complex: Complex.Factory<T, C>
) : ComplexAddition<T, I, C>,
    ImaginaryAddition<T, I> by BasicImaginaryAddition(arithmetic, imaginary),
    ComplexOnlyAddition<T, C> by BasicComplexOnlyAddition(baseTraits, arithmetic, complex) {
    override fun add(lhs: T, rhs: I): C = complex.ofCartesian(lhs, rhs)
    override fun add(lhs: I, rhs: T): C = complex.ofCartesian(rhs, lhs)
    override fun add(lhs: I, rhs: C): C = complex.ofCartesian(rhs.real, arithmetic.add(lhs.value, rhs.imaginary))
    override fun add(lhs: C, rhs: I): C = complex.ofCartesian(lhs.real, arithmetic.add(lhs.imaginary, rhs.value))

    override fun subtract(lhs: T, rhs: I): C = complex.ofCartesian(lhs, baseTraits.negate(rhs.value))
    override fun subtract(lhs: I, rhs: T): C = complex.ofCartesian(baseTraits.negate(rhs), lhs)
    override fun subtract(lhs: I, rhs: C): C =
        complex.ofCartesian(baseTraits.negate(rhs.real), arithmetic.subtract(lhs.value, rhs.imaginary))
    override fun subtract(lhs: C, rhs: I): C =
        complex.ofCartesian(lhs.real, arithmetic.subtract(lhs.imaginary, rhs.value))
}
