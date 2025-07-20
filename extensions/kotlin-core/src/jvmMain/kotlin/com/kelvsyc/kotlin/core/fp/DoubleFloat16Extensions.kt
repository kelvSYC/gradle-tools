package com.kelvsyc.kotlin.core.fp

import com.kelvsyc.kotlin.core.Float16

/**
 * Adds the supplied value to this value.
 */
operator fun Float16.plus(rhs: DoubleFloat16): DoubleFloat16 = DoubleFloat16.Addition.twoSum(rhs, this)

/**
 * Adds the supplied value to this value.
 */
operator fun Float16.minus(rhs: DoubleFloat16): DoubleFloat16 = DoubleFloat16.Addition.twoSum(-rhs, this)

/**
 * Multiplies this value by the supplied value.
 */
operator fun Float16.times(rhs: DoubleFloat16): DoubleFloat16 = DoubleFloat16.Multiplication.twoProduct(rhs, this)

/**
 * Divides this value by the supplied value.
 */
operator fun Float16.div(rhs: DoubleFloat16): DoubleFloat16 =
    DoubleFloat16.Multiplication.twoDivide(DoubleFloat16.of(this), rhs)
