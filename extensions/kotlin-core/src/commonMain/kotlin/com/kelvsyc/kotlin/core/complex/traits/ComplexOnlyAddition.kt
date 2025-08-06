package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.Addition

/**
 * Trait interface denoting addition and subtraction operations on a [Complex] type, backed by a floating-point type.
 *
 * Note that this trait does not extend [Addition]. It is intended that implementations implement the addition of two
 * real values through delegation to an [Addition] implementation.
 */
interface ComplexOnlyAddition<T, C : Complex<T>> {
    fun add(lhs: T, rhs: T): T
    fun add(lhs: C, rhs: C): C

    fun add(lhs: T, rhs: C): C
    fun add(lhs: C, rhs: T): C

    fun subtract(lhs: T, rhs: T): T
    fun subtract(lhs: C, rhs: C): C

    fun subtract(lhs: T, rhs: C): C
    fun subtract(lhs: C, rhs: T): C
}
