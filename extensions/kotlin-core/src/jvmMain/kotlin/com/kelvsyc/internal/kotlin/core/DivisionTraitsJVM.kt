package com.kelvsyc.internal.kotlin.core

import com.google.common.math.BigIntegerMath
import com.kelvsyc.kotlin.core.traits.Division
import com.kelvsyc.kotlin.core.traits.IntegerDivision
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object BigIntegerDivision : IntegerDivision<BigInteger> {
    override fun divide(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs / rhs
    override fun floorDiv(lhs: BigInteger, rhs: BigInteger): BigInteger =
        BigIntegerMath.divide(lhs, rhs, RoundingMode.FLOOR)
    override fun ceilDiv(lhs: BigInteger, rhs: BigInteger): BigInteger =
        BigIntegerMath.divide(lhs, rhs, RoundingMode.CEILING)

    override fun rem(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs.rem(rhs)
    override fun mod(lhs: BigInteger, rhs: BigInteger): BigInteger = lhs.mod(rhs).let {
        // BigInteger.mod() returns a non-negative result, which means it might be a different sign than rhs
        if (it != BigInteger.ZERO && rhs < BigInteger.ZERO) {
            // Force the remainder to be negative
            it + rhs
        } else {
            it
        }
    }
}

object BigDecimalDivision : Division<BigDecimal> {
    override fun divide(lhs: BigDecimal, rhs: BigDecimal): BigDecimal = lhs / rhs
}
