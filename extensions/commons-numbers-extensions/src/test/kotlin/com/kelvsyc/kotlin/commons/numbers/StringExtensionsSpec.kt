package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

class StringExtensionsSpec : FunSpec() {
    init {
        test("toFraction") {
            mockkStatic(Fraction::class) {
                val value = "1/2"
                value.toFraction()
                verify {
                    Fraction.parse(value)
                }
            }
        }
        test("toFractionOrNull invalid") {
            val value = "foobar"
            val result = value.toFractionOrNull()
            result.shouldBeNull()
        }

        test("toBigFraction") {
            mockkStatic(BigFraction::class) {
                val value = "1/2"
                value.toBigFraction()
                verify {
                    BigFraction.parse(value)
                }
            }
        }
        test("toBigFractionOrNull invalid") {
            val value = "foobar"
            val result = value.toBigFractionOrNull()
            result.shouldBeNull()
        }
    }
}
