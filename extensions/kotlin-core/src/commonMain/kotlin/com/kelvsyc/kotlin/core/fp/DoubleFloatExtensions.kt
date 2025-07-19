package com.kelvsyc.kotlin.core.fp

/**
 * Adds the supplied value to this value.
 */
operator fun Float.plus(rhs: DoubleFloat): DoubleFloat = DoubleFloat.Addition.twoSum(rhs, this)

/**
 * Adds the supplied value to this value.
 */
operator fun Float.minus(rhs: DoubleFloat): DoubleFloat = DoubleFloat.Addition.twoSum(-rhs, this)
