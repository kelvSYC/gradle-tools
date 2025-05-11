package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

class DoubleExtensionsSpec : FunSpec() {
    init {
        test("toDD") {
            mockkStatic(DD::class) {
                val value = 1.0
                value.toDD()
                verify {
                    DD.of(value)
                }
            }
        }
        test("toFraction") {
            mockkStatic(Fraction::class) {
                val value = 1.0
                value.toFraction()
                verify {
                    Fraction.from(value)
                }
            }
        }
        test("toBigFraction") {
            mockkStatic(BigFraction::class) {
                val value = 1.0
                value.toBigFraction()
                verify {
                    BigFraction.from(value)
                }
            }
        }
    }
}
