package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.complex.Complex
import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction
import org.apache.commons.numbers.fraction.Fraction

class DoubleExtensionsSpec : FunSpec() {
    init {
        test("imaginary") {
            checkAll<Double> {
                val value = it.i

                value.real shouldBeEqual 0.0
                value.imaginary shouldBeEqual it
            }
        }

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
        test("subtract Complex") {
            val value = 1.0
            val rhs = mockk<Complex>(relaxed = true)
            value - rhs
            verify { rhs.subtractFrom(value) }
        }
    }
}
