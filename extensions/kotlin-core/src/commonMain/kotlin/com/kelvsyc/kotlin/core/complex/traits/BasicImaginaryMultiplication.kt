package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ImaginaryMultiplication] for an [Imaginary] type.
 */
class BasicImaginaryMultiplication<T, I : Imaginary<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val factory: Imaginary.Factory<T, I>
    ) : ImaginaryMultiplication<T, I> {
    override fun multiply(lhs: I, rhs: I): T = baseTraits.negate(arithmetic.multiply(lhs.value, rhs.value))

    override fun multiply(lhs: T, rhs: I): I = factory.of(arithmetic.multiply(lhs, rhs.value))
    override fun multiply(lhs: I, rhs: T): I = factory.of(arithmetic.multiply(lhs.value, rhs))
}
