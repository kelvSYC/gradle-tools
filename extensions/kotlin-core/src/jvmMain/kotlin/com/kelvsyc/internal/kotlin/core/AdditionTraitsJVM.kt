package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.traits.Addition
import java.math.BigDecimal
import java.math.BigInteger

object BigIntegerAddition : Addition<BigInteger> {
    override fun add(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs + rhs
    override fun subtract(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs - rhs
}

object BigDecimalAddition : Addition<BigDecimal> {
    override fun add(lhs: BigDecimal, rhs: BigDecimal): BigDecimal = lhs + rhs
    override fun subtract(lhs: BigDecimal, rhs: BigDecimal): BigDecimal = lhs - rhs
}
