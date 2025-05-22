package com.kelvsyc.kotlin.commons.lang.math

import org.apache.commons.lang3.math.Fraction

operator fun Fraction.unaryPlus(): Fraction = this

/**
 * Returns a fraction representing the negative of this one.
 *
 * This function is provided to allow for operator overloading.
 *
 * @see Fraction.negate
 */
operator fun Fraction.unaryMinus(): Fraction = negate()

/**
 * Adds this [Fraction] to another.
 *
 * This function is provided to allow for operator overloading.
 *
 * @see Fraction.add
 */
operator fun Fraction.plus(rhs: Fraction): Fraction = add(rhs)

/**
 * Subtracts another fraction from this [Fraction].
 *
 * This function is provided to allow for operator overloading.
 *
 * @see Fraction.subtract
 */
operator fun Fraction.minus(rhs: Fraction): Fraction = subtract(rhs)

/**
 * Multiplies this [Fraction] by another. The returned fraction is in reduced form.
 *
 * This function is provided to allow for operator overloading.
 *
 * @see Fraction.multiplyBy
 */
operator fun Fraction.times(rhs: Fraction): Fraction = multiplyBy(rhs)

/**
 * Divides this [Fraction] by another.
 *
 * This function is provided to allow for operator overloading.
 *
 * @see Fraction.divideBy
 */
operator fun Fraction.div(rhs: Fraction): Fraction = divideBy(rhs)
