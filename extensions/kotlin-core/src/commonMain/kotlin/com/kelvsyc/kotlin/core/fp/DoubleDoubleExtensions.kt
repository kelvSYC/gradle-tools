package com.kelvsyc.kotlin.core.fp

/**
 * Adds the supplied value to this value.
 */
operator fun Double.plus(rhs: DoubleDouble): DoubleDouble = DoubleDouble.Addition.twoSum(rhs, this)

/**
 * Adds the supplied value to this value.
 */
operator fun Double.minus(rhs: DoubleDouble): DoubleDouble = DoubleDouble.Addition.twoSum(-rhs, this)
