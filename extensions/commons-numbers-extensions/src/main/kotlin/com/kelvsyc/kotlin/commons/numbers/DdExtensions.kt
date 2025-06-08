@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.DD

operator fun DD.unaryPlus(): DD = this

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.negate
 */
operator fun DD.unaryMinus(): DD = negate()

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.add
 */
operator fun DD.plus(rhs: Double): DD = add(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.add
 */
operator fun DD.plus(rhs: DD): DD = add(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.subtract
 */
operator fun DD.minus(rhs: Double): DD = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.subtract
 */
operator fun DD.minus(rhs: DD): DD = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.multiply
 */
operator fun DD.times(rhs: Int): DD = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.multiply
 */
operator fun DD.times(rhs: Double): DD = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.multiply
 */
operator fun DD.times(rhs: DD): DD = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.divide
 */
operator fun DD.div(rhs: Double): DD = divide(rhs)

/**
 * Delegate function allowing for operator overload for [DD].
 *
 * @see DD.divide
 */
operator fun DD.div(rhs: DD): DD = divide(rhs)
