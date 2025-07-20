package com.kelvsyc.kotlin.core.fp

/**
 * Adds the supplied value to this value.
 */
operator fun Float.plus(rhs: DoubleFloat): DoubleFloat = DoubleFloat.Addition.twoSum(rhs, this)

/**
 * Adds the supplied value to this value.
 */
operator fun Float.minus(rhs: DoubleFloat): DoubleFloat = DoubleFloat.Addition.twoSum(-rhs, this)

/**
 * Multiplies this value by the supplied value.
 */
operator fun Float.times(rhs: DoubleFloat): DoubleFloat = DoubleFloat.Multiplication.twoProduct(rhs, this)

/**
 * Divides this value by the supplied value.
 */
operator fun Float.div(rhs: DoubleFloat): DoubleFloat =
    DoubleFloat.Multiplication.twoDivide(DoubleFloat.of(this), rhs)
