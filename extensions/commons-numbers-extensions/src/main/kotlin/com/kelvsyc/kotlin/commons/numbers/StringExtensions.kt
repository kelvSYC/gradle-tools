package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

/**
 * Converts this value to a [Fraction].
 *
 * @see Fraction.parse
 */
@Throws(NumberFormatException::class)
fun String.toFraction(): Fraction = Fraction.parse(this)

/**
 * Converts this value to a [Fraction].
 *
 * @return The value as a fraction, or `null` if the value is not in a format compatible with [Fraction.toString].
 * @see Fraction.parse
 */
fun String.toFractionOrNull(): Fraction? = try {
    Fraction.parse(this)
} catch(_: NumberFormatException) {
    null
}

/**
 * Converts this value to a [BigFraction].
 *
 * @see BigFraction.parse
 */
@Throws(NumberFormatException::class)
fun String.toBigFraction(): BigFraction = BigFraction.parse(this)

/**
 * Converts this value to a [BigFraction].
 *
 * @return The value as a fraction, or `null` if the value is not in a format compatible with [BigFraction.toString].
 * @see BigFraction.parse
 */
fun String.toBigFractionOrNull(): BigFraction? = try {
    BigFraction.parse(this)
} catch(_: NumberFormatException) {
    null
}
