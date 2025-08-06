package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.FloatingPoint
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.FusedMultiplyAdd

/**
 * Basic implementation of [ComplexOnlyArithmetic], delegating all functionality to [BasicComplexOnlyAddition],
 * [BasicComplexOnlyMultiplication], and [BasicComplexOnlyDivision].
 */
class BasicComplexOnlyArithmetic<T, C : Complex<T>>(
    baseTraits: FloatingPoint<T>,
    arithmetic: FloatingPointArithmetic<T>,
    fma: FusedMultiplyAdd<T>? = null,
    comparator: Comparator<T>,
    factory: Complex.Factory<T, C>
) : ComplexOnlyArithmetic<T, C>,
    ComplexOnlyAddition<T, C> by BasicComplexOnlyAddition(baseTraits, arithmetic, factory),
    ComplexOnlyMultiplication<T, C> by BasicComplexOnlyMultiplication(arithmetic, factory),
    ComplexOnlyDivision<T, C> by BasicComplexOnlyDivision(baseTraits, arithmetic, fma, comparator, factory)
