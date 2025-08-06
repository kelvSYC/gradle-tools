package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.FloatingPointDivision

/**
 * Trait interface denoting division operations on a [Complex] type, backed by a floating-point type.
 *
 * Note that this trait does not extend [FloatingPointDivision]. It is intended that implementations implement the
 * division of two real values through delegation to a [FloatingPointDivision] implementation.
 */
interface ComplexOnlyDivision<T, C : Complex<T>> {
    fun divide(lhs: T, rhs: T): T
    fun divide(lhs: C, rhs: C): C

    fun divide(lhs: T, rhs: C): C
    fun divide(lhs: C, rhs: T): C
}
