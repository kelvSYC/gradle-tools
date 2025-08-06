package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex

/**
 * Marker trait interface supports arithmetic operations on a [Complex] type.
 */
interface ComplexOnlyArithmetic<T, C : Complex<T>> :
    ComplexOnlyAddition<T, C>,
    ComplexOnlyMultiplication<T, C>,
    ComplexOnlyDivision<T, C>
