package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.traits.Division
import java.math.BigDecimal
import java.math.BigInteger

object BigIntegerDivision : Division<BigInteger> {
    override fun divide(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs / rhs
}

object BigDecimalDivision : Division<BigDecimal> {
    override fun divide(lhs: BigDecimal, rhs: BigDecimal): BigDecimal = lhs / rhs
}
