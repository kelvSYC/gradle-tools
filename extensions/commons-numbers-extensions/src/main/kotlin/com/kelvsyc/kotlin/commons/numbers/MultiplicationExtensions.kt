package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.Multiplication

/**
 * Delegate function allowing for operator overload for the multipliable type.
 *
 * @see Multiplication.multiply
 */
operator fun <T : Multiplication<T>> T.times(rhs: T): T = multiply(rhs)

/**
 * Function implementing operator overload for the multipliable type.
 *
 * Equivalent to [multiply][Multiplication.multiply]`(rhs(`[reciprocal][Multiplication.reciprocal]`()))`
 */
operator fun <T : Multiplication<T>> T.div(rhs: T): T = multiply(rhs.reciprocal())
