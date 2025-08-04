package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.traits.Signed
import java.math.BigDecimal
import java.math.BigInteger

object BigIntegerSigned : Signed<BigInteger> {
    override fun isPositive(value: BigInteger): Boolean = value.signum() == 1
    override fun isNegative(value: BigInteger): Boolean = value.signum() == -1
    override fun negate(value: BigInteger): BigInteger = -value
    override fun absoluteValue(value: BigInteger): BigInteger = value.abs()
}

object BigDecimalSigned : Signed<BigDecimal> {
    override fun isPositive(value: BigDecimal): Boolean = value.signum() == 1
    override fun isNegative(value: BigDecimal): Boolean = value.signum() == -1
    override fun negate(value: BigDecimal): BigDecimal = -value
    override fun absoluteValue(value: BigDecimal): BigDecimal = value.abs()
}
