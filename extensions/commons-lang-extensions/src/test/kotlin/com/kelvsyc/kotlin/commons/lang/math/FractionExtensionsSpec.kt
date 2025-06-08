package com.kelvsyc.kotlin.commons.lang.math

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.math.Fraction

class FractionExtensionsSpec : FunSpec() {
    init {
        test("Unary negate") {
            val value = mockk<Fraction>(relaxed = true)
            -value
            verify { value.negate() }
        }

        test("Addition") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.getFraction(1, 2)
            value + rhs
            verify { value.add(rhs) }
        }

        test("Subtraction") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.getFraction(1, 2)
            value - rhs
            verify { value.subtract(rhs) }
        }

        test("Multiplication") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.getFraction(1, 2)
            value * rhs
            verify { value.multiplyBy(rhs) }
        }

        test("Division") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.getFraction(1, 2)
            value / rhs
            verify { value.divideBy(rhs) }
        }
    }
}
