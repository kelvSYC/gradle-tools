package com.kelvsyc.kotlin.commons.numbers.complex

import io.kotest.core.spec.style.FunSpec
import io.kotest.data.blocking.forAll
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.complex.Complex

class ComplexExtensionsSpec : FunSpec() {
    init {
        test("destructure") {
            forAll<Complex> {
                val (re, im) = it

                re shouldBeEqual it.real
                im shouldBeEqual it.imaginary
            }
        }

        test("negate") {
            val value = mockk<Complex>(relaxed = true)
            -value
            verify { value.negate() }
        }

        test("add double") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = 0.0
            value + rhs
            verify { value.add(rhs) }
        }
        test("add Complex") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = Complex.ZERO
            value + rhs
            verify { value.add(rhs) }
        }
        test("subtract double") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = 0.0
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("subtract Complex") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = Complex.ZERO
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("multiply double") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = 1.0
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply Complex") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = Complex.ZERO
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("divide double") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = 1.0
            value / rhs
            verify { value.divide(rhs) }
        }
        test("divide Complex") {
            val value = mockk<Complex>(relaxed = true)
            val rhs = Complex.ONE
            value / rhs
            verify { value.divide(rhs) }
        }
    }
}
