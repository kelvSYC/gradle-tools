package com.kelvsyc.kotlin.commons.numbers.fraction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.fraction.Fraction

class FractionExtensionsSpec : FunSpec() {
    init {
        test("destructure") {
            checkAll<Fraction>(arbitraryFraction) {
                val (numerator, denominator) = it
                numerator shouldBeEqual it.numerator
                denominator shouldBeEqual it.denominator
            }
        }

        test("negate") {
            val value = mockk<Fraction>(relaxed = true)
            -value
            verify { value.negate() }
        }

        test("add int") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = 0
            value + rhs
            verify { value.add(rhs) }
        }
        test("add Fraction") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.ZERO
            value + rhs
            verify { value.add(rhs) }
        }
        test("subtract int") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = 0
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("subtract Fraction") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.ZERO
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("multiply int") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = 1
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply Fraction") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.ZERO
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("divide int") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = 1
            value / rhs
            verify { value.divide(rhs) }
        }
        test("divide Fraction") {
            val value = mockk<Fraction>(relaxed = true)
            val rhs = Fraction.ONE
            value / rhs
            verify { value.divide(rhs) }
        }

        test("toBigFraction") {
            val value = Fraction.of(1, 2)
            val bigValue = value.toBigFraction()

            value.numerator shouldBeEqual bigValue.numeratorAsInt
            value.denominator shouldBeEqual bigValue.denominatorAsInt
        }
    }
}
