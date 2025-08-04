package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.BigDecimalAddition
import com.kelvsyc.internal.kotlin.core.BigDecimalDivision
import com.kelvsyc.internal.kotlin.core.BigDecimalSigned
import com.kelvsyc.internal.kotlin.core.BigIntegerAddition
import com.kelvsyc.internal.kotlin.core.BigIntegerDivision
import com.kelvsyc.internal.kotlin.core.BigIntegerSigned
import com.kelvsyc.kotlin.core.traits.Addition
import com.kelvsyc.kotlin.core.traits.Division
import com.kelvsyc.kotlin.core.traits.Signed
import java.math.BigDecimal as JBigDecimal
import java.math.BigInteger as JBigInteger

/**
 * Object holder for type traits for common Java types.
 */
object JvmTypeTraits {
    object BigInteger : Addition<JBigInteger> by BigIntegerAddition,
        Multiplication<JBigInteger>,
        Division<JBigInteger> by BigIntegerDivision,
        Signed<JBigInteger> by BigIntegerSigned {
        override fun multiply(lhs: JBigInteger, rhs: JBigInteger): JBigInteger = lhs * rhs
    }

    object BigDecimal : Addition<JBigDecimal> by BigDecimalAddition,
        Multiplication<JBigDecimal>,
        Division<JBigDecimal> by BigDecimalDivision,
        Signed<JBigDecimal> by BigDecimalSigned {
        override fun multiply(lhs: JBigDecimal, rhs: JBigDecimal): JBigDecimal = lhs * rhs
    }
}
