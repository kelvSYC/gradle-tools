@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.commons.numbers.fraction

import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

/**
 * Destructuring operator allowing for the extraction of the numerator of a [Fraction].
 */
operator fun Fraction.component1(): Int = numerator

/**
 * Destructuring operator allowing for the extraction of the denominator of a [Fraction].
 */
operator fun Fraction.component2(): Int = denominator

operator fun Fraction.unaryPlus(): Fraction = this

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.negate
 */
operator fun Fraction.unaryMinus(): Fraction = negate()

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.add
 */
operator fun Fraction.plus(rhs: Int): Fraction = add(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.add
 */
operator fun Fraction.plus(rhs: Fraction): Fraction = add(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.subtract
 */
operator fun Fraction.minus(rhs: Int): Fraction = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.subtract
 */
operator fun Fraction.minus(rhs: Fraction) = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.multiply
 */
operator fun Fraction.times(rhs: Int) = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.multiply
 */
operator fun Fraction.times(rhs: Fraction) = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.divide
 */
operator fun Fraction.div(rhs: Int) = divide(rhs)

/**
 * Delegate function allowing for operator overload for [Fraction].
 *
 * @see Fraction.divide
 */
operator fun Fraction.div(rhs: Fraction) = divide(rhs)

/**
 * Converts this value to a [BigFraction].
 */
fun Fraction.toBigFraction() = BigFraction.of(numerator, denominator)
