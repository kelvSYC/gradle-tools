package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ImaginaryAddition].
 */
class BasicImaginaryAddition<T, I : Imaginary<T>>(
    private val arithmetic: FloatingPointArithmetic<T>,
    private val factory: Imaginary.Factory<T, I>
    ) : ImaginaryAddition<T, I> {
    override fun add(lhs: I, rhs: I): I = factory.of(arithmetic.add(lhs.value, rhs.value))

    override fun subtract(lhs: I, rhs: I): I = factory.of(arithmetic.subtract(lhs.value, rhs.value))
}
