package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ImaginaryDivision] for [Imaginary] types.
 */
class BasicImaginaryDivision<T, I : Imaginary<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val arithmetic: FloatingPointArithmetic<T>,
    private val factory: Imaginary.Factory<T, I>,
) : ImaginaryDivision<T, I> {
    override fun divide(lhs: I, rhs: I): T = arithmetic.divide(lhs.value, rhs.value)

    override fun divide(lhs: T, rhs: I): I = factory.of(baseTraits.negate(arithmetic.divide(lhs, rhs.value)))
    override fun divide(lhs: I, rhs: T): I = factory.of(arithmetic.divide(lhs.value, rhs))
}
