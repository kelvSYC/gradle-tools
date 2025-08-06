package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary

interface ImaginaryFloatingPoint<T, I : Imaginary<T>> {
    val zero: I

    val i : I

    fun isInfinite(value: I): Boolean

    fun isNaN(value: I): Boolean

    fun isFinite(value: I): Boolean
}
