package com.kelvsyc.kotlin.guava.math

import com.google.common.math.BigDecimalMath
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Syntactic sugar for [BigDecimalMath.roundToDouble].
 *
 * @see BigDecimalMath.roundToDouble
 */
fun BigDecimal.roundToDouble(mode: RoundingMode) = BigDecimalMath.roundToDouble(this, mode)
