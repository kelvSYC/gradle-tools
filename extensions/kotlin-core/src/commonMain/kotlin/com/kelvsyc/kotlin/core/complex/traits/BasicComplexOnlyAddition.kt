package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ComplexOnlyAddition], where parts are added to each other component-wise.
 */
class BasicComplexOnlyAddition<T, C : Complex<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val factory: Complex.Factory<T, C>
) : ComplexOnlyAddition<T, C> {
    override fun add(lhs: T, rhs: T): T = arithmetic.add(lhs, rhs)
    override fun add(lhs: C, rhs: C): C =
        factory.ofCartesian(arithmetic.add(lhs.real, rhs.real), arithmetic.add(lhs.imaginary, rhs.imaginary))

    override fun add(lhs: T, rhs: C): C = factory.ofCartesian(arithmetic.add(lhs, rhs.real), rhs.imaginary)
    override fun add(lhs: C, rhs: T): C = factory.ofCartesian(arithmetic.add(lhs.real, rhs), lhs.imaginary)

    override fun subtract(lhs: T, rhs: T): T = arithmetic.subtract(lhs, rhs)
    override fun subtract(lhs: C, rhs: C): C =
        factory.ofCartesian(arithmetic.subtract(lhs.real, rhs.real), arithmetic.subtract(lhs.imaginary, rhs.imaginary))
    override fun subtract(lhs: T, rhs: C): C =
        factory.ofCartesian(arithmetic.subtract(lhs, rhs.real), baseTraits.negate(rhs.imaginary))
    override fun subtract(lhs: C, rhs: T): C = factory.ofCartesian(arithmetic.subtract(lhs.real, rhs), lhs.imaginary)
}
