package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.FusedMultiplyAdd

/**
 * Basic implementation of [ComplexArithmetic], delegating all functionality to [BasicComplexAddition],
 * [BasicComplexMultiplication], and [BasicComplexDivision].
 */
class BasicComplexArithmetic<T, I : Imaginary<T>, C : Complex<T>>(
    baseTraits: FloatingPoint<T>,
    arithmetic: FloatingPointArithmetic<T>,
    fma: FusedMultiplyAdd<T>? = null,
    comparator: Comparator<T>,
    imaginary: Imaginary.Factory<T, I>,
    complex: Complex.Factory<T, C>
) : ComplexArithmetic<T, I, C>,
    ComplexAddition<T, I, C> by BasicComplexAddition(baseTraits, arithmetic, imaginary, complex),
    ComplexMultiplication<T, I, C> by BasicComplexMultiplication(baseTraits, arithmetic, imaginary, complex),
    ComplexDivision<T, I, C> by BasicComplexDivision(baseTraits, arithmetic, fma, comparator, imaginary, complex)
