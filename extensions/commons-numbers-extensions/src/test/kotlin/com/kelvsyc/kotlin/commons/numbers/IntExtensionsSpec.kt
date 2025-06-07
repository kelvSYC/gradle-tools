package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

class IntExtensionsSpec : FunSpec() {
    init {
        test("toDD") {
            mockkStatic(DD::class) {
                val value = 1
                value.toDD()
                verify {
                    DD.of(value)
                }
            }
        }
        test("toFraction") {
            mockkStatic(Fraction::class) {
                val value = 1
                value.toFraction()
                verify {
                    Fraction.of(value)
                }
            }
        }
        test("toBigFraction") {
            mockkStatic(BigFraction::class) {
                val value = 1
                value.toBigFraction()
                verify {
                    BigFraction.of(value)
                }
            }
        }
    }
}
