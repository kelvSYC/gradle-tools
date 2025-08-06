package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic

/**
 * Basic implementation of [ImaginaryArithmetic], delegating all functionality to [BasicImaginaryAddition],
 * [BasicImaginaryMultiplication], and [BasicImaginaryDivision].
 */
class BasicImaginaryArithmetic<T, I : Imaginary<T>>(
    baseTraits: FloatingPoint<T>,
    arithmetic: FloatingPointArithmetic<T>,
    factory: Imaginary.Factory<T, I>
) : ImaginaryArithmetic<T, I>,
    ImaginaryAddition<T, I> by BasicImaginaryAddition(arithmetic, factory),
    ImaginaryMultiplication<T, I> by BasicImaginaryMultiplication(baseTraits, arithmetic, factory),
    ImaginaryDivision<T, I> by BasicImaginaryDivision(baseTraits, arithmetic, factory)
