package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.traits.Multiplication
import java.math.BigDecimal
import java.math.BigInteger

object BigIntegerMultiplication : Multiplication<BigInteger> {
    override fun multiply(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs * rhs
}

object BigDecimalMultiplication : Multiplication<BigDecimal> {
    override fun multiply(lhs: BigDecimal, rhs: BigDecimal): BigDecimal = lhs * rhs
}
