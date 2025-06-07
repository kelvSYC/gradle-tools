package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction

/**
 * Delegating function converting this value to a [DD].
 *
 * @see DD.of
 */
fun Long.toDD(): DD = DD.of(this)

/**
 * Delegating function converting this value to a [BigFraction].
 *
 * @see BigFraction.of
 */
fun Long.toBigFraction(): BigFraction = BigFraction.of(this)
