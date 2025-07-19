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
