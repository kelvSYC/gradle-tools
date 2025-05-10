package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.NativeOperators

/**
 * Delegate function allowing for operator overload for the arithmetic type.
 *
 * @see NativeOperators.subtract
 */
operator fun <T : NativeOperators<T>> T.minus(rhs: T): T = subtract(rhs)

/**
 * Delegate function allowing for operator overload for the arithmetic type.
 *
 * @see NativeOperators.multiply
 */
operator fun <T : NativeOperators<T>> T.times(rhs: Int): T = multiply(rhs)

/**
 * Delegate function allowing for operator overload for the arithmetic type.
 *
 * @see NativeOperators.divide
 */
operator fun <T : NativeOperators<T>> T.div(rhs: T): T = divide(rhs)
