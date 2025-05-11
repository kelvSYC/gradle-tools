package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.complex.Complex
import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

/**
 * Delegating function converting this value to a [DD].
 *
 * @see DD.of
 */
fun Double.toDD(): DD = DD.of(this)

/**
 * Delegating function converting this value to a [Fraction].
 *
 * @see Fraction.from
 */
fun Double.toFraction(): Fraction = Fraction.from(this)

/**
 * Delegating function converting this value to a [BigFraction].
 *
 * @see BigFraction.from
 */
fun Double.toBigFraction(): BigFraction = BigFraction.from(this)

/**
 * Delegate function allowing for operator overload for [Complex].
 *
 * @see Complex.subtractFrom
 */
operator fun Double.minus(rhs: Complex): Complex = rhs.subtractFrom(this)
