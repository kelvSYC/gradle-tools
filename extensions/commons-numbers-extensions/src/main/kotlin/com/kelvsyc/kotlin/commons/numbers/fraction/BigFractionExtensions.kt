@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.commons.numbers.fraction

import org.apache.commons.numbers.fraction.BigFraction
import java.math.BigInteger

/**
 * Destructuring operator allowing for the extraction of the numerator of a [BigFraction].
 */
operator fun BigFraction.component1(): BigInteger = numerator

/**
 * Destructuring operator allowing for the extraction of the denominator of a [BigFraction].
 */
operator fun BigFraction.component2(): BigInteger = denominator

operator fun BigFraction.unaryPlus(): BigFraction = this

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.negate
 */
operator fun BigFraction.unaryMinus(): BigFraction = negate()

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.add
 */
operator fun BigFraction.plus(rhs: Int): BigFraction = add(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.add
 */
operator fun BigFraction.plus(rhs: Long): BigFraction = add(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.add
 */
operator fun BigFraction.plus(rhs: BigInteger): BigFraction = add(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.add
 */
operator fun BigFraction.plus(rhs: BigFraction): BigFraction = add(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.subtract
 */
operator fun BigFraction.minus(rhs: Int): BigFraction = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.subtract
 */
operator fun BigFraction.minus(rhs: Long): BigFraction = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.subtract
 */
operator fun BigFraction.minus(rhs: BigInteger): BigFraction = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.subtract
 */
operator fun BigFraction.minus(rhs: BigFraction) = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.multiply
 */
operator fun BigFraction.times(rhs: Int) = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.multiply
 */
operator fun BigFraction.times(rhs: Long) = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.multiply
 */
operator fun BigFraction.times(rhs: BigInteger) = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.multiply
 */
operator fun BigFraction.times(rhs: BigFraction) = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.divide
 */
operator fun BigFraction.div(rhs: Int) = divide(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.divide
 */
operator fun BigFraction.div(rhs: Long) = divide(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.divide
 */
operator fun BigFraction.div(rhs: BigInteger) = divide(rhs)

/**
 * Delegate function allowing for operator overload for [BigFraction].
 *
 * @see BigFraction.divide
 */
operator fun BigFraction.div(rhs: BigFraction) = divide(rhs)
