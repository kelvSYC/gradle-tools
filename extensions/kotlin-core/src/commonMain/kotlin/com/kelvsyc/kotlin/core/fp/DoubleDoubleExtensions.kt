package com.kelvsyc.kotlin.core.fp

/**
 * Adds the supplied value to this value.
 */
operator fun Double.plus(rhs: DoubleDouble): DoubleDouble = DoubleDouble.Addition.twoSum(rhs, this)

/**
 * Adds the supplied value to this value.
 */
operator fun Double.minus(rhs: DoubleDouble): DoubleDouble = DoubleDouble.Addition.twoSum(-rhs, this)

/**
 * Multiplies this value by the supplied value.
 */
operator fun Double.times(rhs: DoubleDouble): DoubleDouble = DoubleDouble.Multiplication.twoProduct(rhs, this)

/**
 * Divides this value by the supplied value.
 */
operator fun Double.div(rhs: DoubleDouble): DoubleDouble =
    DoubleDouble.Multiplication.twoDivide(DoubleDouble.of(this), rhs)
