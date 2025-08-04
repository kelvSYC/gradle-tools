package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.BigDecimalSigned
import com.kelvsyc.internal.kotlin.core.BigIntegerSigned
import com.kelvsyc.kotlin.core.traits.Signed
import java.math.BigDecimal as JBigDecimal
import java.math.BigInteger as JBigInteger

/**
 * Object holder for type traits for common Java types.
 */
object JvmTypeTraits {
    object BigInteger : Addition<JBigInteger>, Multiplication<JBigInteger>,
        Signed<JBigInteger> by BigIntegerSigned {
        override fun add(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs + rhs
        override fun subtract(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs - rhs

        override fun multiply(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs * rhs
        override fun divide(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs / rhs
    }

    object BigDecimal : Addition<JBigDecimal>, Multiplication<JBigDecimal>,
        Signed<JBigDecimal> by BigDecimalSigned {
        override fun add(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs + rhs
        override fun subtract(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs - rhs

        override fun multiply(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs * rhs
        override fun divide(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs / rhs
    }
}
