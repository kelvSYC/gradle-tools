package com.kelvsyc.kotlin.core

import java.math.BigDecimal as JBigDecimal
import java.math.BigInteger as JBigInteger

/**
 * Object holder for type traits for common Java types.
 */
object JvmTypeTraits {
    object BigInteger : Addition<JBigInteger>, Multiplication<JBigInteger>, Signed<JBigInteger> {
        override fun add(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs + rhs
        override fun subtract(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs - rhs

        override fun multiply(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs * rhs
        override fun divide(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs / rhs

        override fun isPositive(value: JBigInteger): Boolean = value.signum() == 1
        override fun isNegative(value: JBigInteger): Boolean = value.signum() == -1
        override fun negate(value: JBigInteger): JBigInteger = -value
        override fun absoluteValue(value: JBigInteger): JBigInteger = value.abs()
    }

    object BigDecimal : Addition<JBigDecimal>, Multiplication<JBigDecimal>, Signed<JBigDecimal> {
        override fun add(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs + rhs
        override fun subtract(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs - rhs

        override fun multiply(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs * rhs
        override fun divide(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs / rhs

        override fun isPositive(value: JBigDecimal): Boolean = value.signum() == 1
        override fun isNegative(value: JBigDecimal): Boolean = value.signum() == -1
        override fun negate(value: JBigDecimal): JBigDecimal = -value
        override fun absoluteValue(value: JBigDecimal): JBigDecimal = value.abs()
    }
}
