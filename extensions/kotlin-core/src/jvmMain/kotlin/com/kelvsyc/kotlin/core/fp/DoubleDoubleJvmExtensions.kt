package com.kelvsyc.kotlin.core.fp

import java.math.BigDecimal
import java.math.MathContext

/**
 * Returns the value of this [DoubleDouble] as a [BigDecimal].
 *
 * Both of the [high][DoubleDouble.high] and [low][DoubleDouble.low] components will be individually converted to
 * [BigDecimal] before being added. Some loss of precision may occur due to the inherent conversion from [Double] to
 * [BigDecimal] being handled through a [String] intermediary.
 */
fun DoubleDouble.toBigDecimal(): BigDecimal = high.toBigDecimal() + low.toBigDecimal()

/**
 * Returns the value of this [DoubleDouble] as a [BigDecimal].
 *
 * Both of the [high][DoubleDouble.high] and [low][DoubleDouble.low] components will be individually converted to
 * [BigDecimal] before being added. Some loss of precision may occur due to the inherent conversion from [Double] to
 * [BigDecimal] being handled through a [String] intermediary.
 *
 * @param context Specifies the precision and rounding mode.
 */
fun DoubleDouble.toBigDecimal(context: MathContext) = high.toBigDecimal(context) + low.toBigDecimal(context)
