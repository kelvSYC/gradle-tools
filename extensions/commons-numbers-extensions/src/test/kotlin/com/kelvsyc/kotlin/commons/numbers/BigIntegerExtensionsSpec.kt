package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.fraction.BigFraction
import java.math.BigInteger

class BigIntegerExtensionsSpec : FunSpec() {
    init {
        test("toBigFraction") {
            mockkStatic(BigFraction::class) {
                val value = BigInteger.ONE
                value.toBigFraction()
                verify {
                    BigFraction.of(value)
                }
            }
        }
    }
}
