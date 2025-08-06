package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.Addition

/**
 * Trait interface denoting addition and subtraction operations on a [Complex] and [Imaginary] type, each backed by
 * a floating-point type.
 *
 * Note that this trait does not extend [Addition]. It is intended that implementations implement the addition of two
 * real values through delegation to an [Addition] implementation.
 */
@Suppress("detekt:TooManyFunctions")
interface ComplexAddition<T, I : Imaginary<T>, C : Complex<T>> : ImaginaryAddition<T, I>, ComplexOnlyAddition<T, C> {
    fun add(lhs: T, rhs: I): C
    fun add(lhs: I, rhs: T): C
    fun add(lhs: I, rhs: C): C
    fun add(lhs: C, rhs: I): C

    fun subtract(lhs: T, rhs: I): C
    fun subtract(lhs: I, rhs: T): C
    fun subtract(lhs: I, rhs: C): C
    fun subtract(lhs: C, rhs: I): C
}
