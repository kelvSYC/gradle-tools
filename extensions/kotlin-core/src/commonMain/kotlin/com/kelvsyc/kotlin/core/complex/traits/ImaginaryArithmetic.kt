package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary

/**
 * Marker trait interface supports arithmetic operations on an [Imaginary] type.
 */
interface ImaginaryArithmetic<T, I : Imaginary<T>> :
    ImaginaryAddition<T, I>,
    ImaginaryMultiplication<T, I>,
    ImaginaryDivision<T, I>
