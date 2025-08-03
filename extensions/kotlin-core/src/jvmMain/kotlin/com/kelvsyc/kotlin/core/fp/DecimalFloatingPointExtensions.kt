package com.kelvsyc.kotlin.core.fp

import java.math.BigDecimal
import java.math.MathContext

// TODO Find a way to do this for any arbitrary DecimalFloatingPoint.Finite<B>

@JvmName("intDecimalFloatingPointToBigDecimal")
fun DecimalFloatingPoint.Finite<Int>.toBigDecimal(context: MathContext? = null): BigDecimal {
    val unscaled = significand.toBigInteger().let { if (signBit) it.negate() else it }
    if (context == null) {
        return BigDecimal(unscaled, -exponent)
    } else {
        return BigDecimal(unscaled, -exponent, context)
    }
}

@JvmName("longDecimalFloatingPointToBigDecimal")
fun DecimalFloatingPoint.Finite<Long>.toBigDecimal(context: MathContext? = null): BigDecimal {
    val unscaled = significand.toBigInteger().let { if (signBit) it.negate() else it }
    if (context == null) {
        return BigDecimal(unscaled, -exponent)
    } else {
        return BigDecimal(unscaled, -exponent, context)
    }
}
