package com.kelvsyc.kotlin.core.fp

import java.math.BigDecimal
import java.math.MathContext

/**
 * Returns the value of this [DoubleFloat] as a [BigDecimal].
 *
 * Both of the [high][DoubleFloat.high] and [low][DoubleFloat.low] components will be individually converted to
 * [BigDecimal] before being added. Some loss of precision may occur due to the inherent conversion from [Float] to
 * [BigDecimal] being handled through a [String] intermediary.
 */
fun DoubleFloat.toBigDecimal(): BigDecimal = high.toBigDecimal() + low.toBigDecimal()

/**
 * Returns the value of this [DoubleFloat] as a [BigDecimal].
 *
 * Both of the [high][DoubleFloat.high] and [low][DoubleFloat.low] components will be individually converted to
 * [BigDecimal] before being added. Some loss of precision may occur due to the inherent conversion from [Float] to
 * [BigDecimal] being handled through a [String] intermediary.
 *
 * @param context Specifies the precision and rounding mode.
 */
fun DoubleFloat.toBigDecimal(context: MathContext): BigDecimal = high.toBigDecimal(context) + low.toBigDecimal(context)
