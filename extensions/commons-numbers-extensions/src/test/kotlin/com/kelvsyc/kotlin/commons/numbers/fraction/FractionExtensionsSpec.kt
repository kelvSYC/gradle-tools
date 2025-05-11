package com.kelvsyc.kotlin.commons.numbers.fraction

import com.kelvsyc.kotlin.commons.numbers.div
import com.kelvsyc.kotlin.commons.numbers.minus
import com.kelvsyc.kotlin.commons.numbers.plus
import com.kelvsyc.kotlin.commons.numbers.times
import com.kelvsyc.kotlin.commons.numbers.unaryMinus
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.fraction.Fraction

class FractionExtensionsSpec : FunSpec() {
    init {
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
    }
}
