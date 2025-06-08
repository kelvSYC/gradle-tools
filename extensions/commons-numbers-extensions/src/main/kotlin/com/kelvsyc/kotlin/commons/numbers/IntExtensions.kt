package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

/**
 * Delegating function converting this value to a [DD].
 *
 * @see DD.of
 */
fun Int.toDD(): DD = DD.of(this)

/**
 * Delegating function converting this value to a [Fraction].
 *
 * @see Fraction.of
 */
fun Int.toFraction(): Fraction = Fraction.of(this)

/**
 * Delegating function converting this value to a [BigFraction].
 *
 * @see BigFraction.of
 */
fun Int.toBigFraction(): BigFraction = BigFraction.of(this)
