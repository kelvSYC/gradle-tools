package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.fraction.BigFraction
import java.math.BigInteger

/**
 * Delegating function converting this value to a [BigFraction].
 *
 * @see BigFraction.of
 */
fun BigInteger.toBigFraction(): BigFraction = BigFraction.of(this)
