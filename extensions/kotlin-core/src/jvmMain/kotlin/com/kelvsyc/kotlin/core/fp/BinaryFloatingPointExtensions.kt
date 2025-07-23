package com.kelvsyc.kotlin.core.fp

import java.math.BigDecimal
import java.math.MathContext

// TODO find out if we can do this generically for an arbitrary BinaryFloatingPoint.Finite<B>

/**
 * Converts this floating-point value to a [BigDecimal], with optional rounding according to the context settings.
 */
@JvmName("intBinaryFloatingPointToBigDecimal")
fun BinaryFloatingPoint.Finite<Int>.toBigDecimal(context: MathContext? = null): BigDecimal {
    // TODO make sure that we are exact (use MathContext.UNLIMITED) before applying the specified context at the end
    val abs = BigDecimal.TWO.pow(exponent) * BigDecimal(significand)
    return if (signBit) {
        if (context == null) abs.negate() else abs.negate(context)
    } else {
        if (context == null) abs else abs.round(context)
    }
}

/**
 * Converts this floating-point value to a [BigDecimal], with optional rounding according to the context settings.
 */
@JvmName("longBinaryFloatingPointToBigDecimal")
fun BinaryFloatingPoint.Finite<Long>.toBigDecimal(context: MathContext? = null): BigDecimal {
    // TODO make sure that we are exact (use MathContext.UNLIMITED) before applying the specified context at the end
    val abs = BigDecimal.TWO.pow(exponent) * BigDecimal(significand)
    return if (signBit) {
        if (context == null) abs.negate() else abs.negate(context)
    } else {
        if (context == null) abs else abs.round(context)
    }
}
