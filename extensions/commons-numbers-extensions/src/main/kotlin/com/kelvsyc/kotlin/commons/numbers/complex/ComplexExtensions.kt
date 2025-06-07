@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.commons.numbers.complex

import org.apache.commons.numbers.complex.Complex

/**
 * Destructuring operator allowing for the extraction of the real part of a [Complex].
 */
operator fun Complex.component1(): Double = real

/**
 * Destructuring operator allowing for the extraction of the imaginary part of a [Complex].
 */
operator fun Complex.component2(): Double = imaginary

operator fun Complex.unaryPlus(): Complex = this

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.negate
 */
operator fun Complex.unaryMinus(): Complex = negate()

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.add
 */
operator fun Complex.plus(rhs: Double): Complex = add(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.add
 */
operator fun Complex.plus(rhs: Complex): Complex = add(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.subtract
 */
operator fun Complex.minus(rhs: Double): Complex = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.subtract
 */
operator fun Complex.minus(rhs: Complex): Complex = subtract(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.multiply
 */
operator fun Complex.times(rhs: Double): Complex = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.multiply
 */
operator fun Complex.times(rhs: Complex): Complex = multiply(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.divide
 */
operator fun Complex.div(rhs: Double): Complex = divide(rhs)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.divide
 */
operator fun Complex.div(rhs: Complex): Complex = divide(rhs)
