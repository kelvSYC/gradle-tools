package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary

/**
 * Marker trait interface supports arithmetic operations on [Complex] and [Imaginary] types.
 */
interface ComplexArithmetic<T, I : Imaginary<T>, C : Complex<T>> :
    ImaginaryArithmetic<T, I>,
    ComplexOnlyArithmetic<T, C>,
    ComplexAddition<T, I, C>,
    ComplexMultiplication<T, I, C>,
    ComplexDivision<T, I, C>
