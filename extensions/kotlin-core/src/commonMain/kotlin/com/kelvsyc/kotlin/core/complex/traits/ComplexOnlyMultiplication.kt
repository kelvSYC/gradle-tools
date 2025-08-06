package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.Multiplication

/**
 * Trait interface denoting multiplication operations on a [Complex] type, backed by a floating-point type.
 *
 * Note that this trait does not extend [Multiplication]. It is intended that implementations implement the
 * multiplication of two real values through delegation to a [Multiplication] implementation.
 */
interface ComplexOnlyMultiplication<T, C : Complex<T>> {
    fun multiply(lhs: T, rhs: T): T
    fun multiply(lhs: C, rhs: C): C

    fun multiply(lhs: T, rhs: C): C
    fun multiply(lhs: C, rhs: T): C
}
