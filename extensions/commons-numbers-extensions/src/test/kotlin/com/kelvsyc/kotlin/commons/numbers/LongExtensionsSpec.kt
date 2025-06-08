package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.numbers.core.DD
import org.apache.commons.numbers.fraction.BigFraction

class LongExtensionsSpec : FunSpec() {
    init {
        test("toDD") {
            mockkStatic(DD::class) {
                val value = 1L
                value.toDD()
                verify {
                    DD.of(value)
                }
            }
        }
        test("toBigFraction") {
            mockkStatic(BigFraction::class) {
                val value = 1L
                value.toBigFraction()
                verify {
                    BigFraction.of(value)
                }
            }
        }
    }
}
