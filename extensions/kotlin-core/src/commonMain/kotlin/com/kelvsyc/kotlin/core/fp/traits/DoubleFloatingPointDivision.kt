package com.kelvsyc.kotlin.core.fp.traits

import com.kelvsyc.kotlin.core.fp.DoubleFloatingPoint
import com.kelvsyc.kotlin.core.traits.Division

/**
 * Traits interface defining division operations on a double floating-point type [D], created from two non-overlapping
 * scalars of type [F].
 *
 * No implementation of division operations can be exact. Rather, the relative error from exact can be kept under a
 * certain bound, which is implementation-dependent.
 */
interface DoubleFloatingPointDivision<F, D : DoubleFloatingPoint<F>> : Division<D> {
    /**
     * Divides a doubled value by a scalar value.
     */
    fun divide(lhs: D, rhs: F): D

    /**
     * Divides a doubled value by another doubled value.
     */
    override fun divide(lhs: D, rhs: D): D
}
