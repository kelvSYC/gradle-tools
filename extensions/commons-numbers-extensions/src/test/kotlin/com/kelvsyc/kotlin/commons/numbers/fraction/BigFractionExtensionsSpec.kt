package com.kelvsyc.kotlin.commons.numbers.fraction

import com.kelvsyc.kotlin.commons.numbers.div
import com.kelvsyc.kotlin.commons.numbers.minus
import com.kelvsyc.kotlin.commons.numbers.plus
import com.kelvsyc.kotlin.commons.numbers.times
import com.kelvsyc.kotlin.commons.numbers.unaryMinus
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.blocking.forAll
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.fraction.BigFraction
import java.math.BigInteger

class BigFractionExtensionsSpec : FunSpec() {
    init {
        test("destructure") {
            forAll<BigFraction> {
                val (numerator, denominator) = it
                numerator shouldBeEqual it.numerator
                denominator shouldBeEqual it.denominator
            }
        }

        test("negate") {
            val value = mockk<BigFraction>(relaxed = true)
            -value
            verify { value.negate() }
        }

        test("add int") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 0
            value + rhs
            verify { value.add(rhs) }
        }
        test("add long") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 0L
            value + rhs
            verify { value.add(rhs) }
        }
        test("add BigInteger") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigInteger.ZERO
            value + rhs
            verify { value.add(rhs) }
        }
        test("add BigFraction") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigFraction.ZERO
            value + rhs
            verify { value.add(rhs) }
        }

        test("subtract int") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 0
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("subtract long") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 0L
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("subtract BigInteger") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigInteger.ZERO
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("subtract BigFraction") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigFraction.ZERO
            value - rhs
            verify { value.subtract(rhs) }
        }

        test("multiply int") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 1
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply long") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 1L
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply BigInteger") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigInteger.ONE
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply BigFraction") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigFraction.ZERO
            value * rhs
            verify { value.multiply(rhs) }
        }

        test("divide int") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 1
            value / rhs
            verify { value.divide(rhs) }
        }
        test("divide long") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = 1L
            value / rhs
            verify { value.divide(rhs) }
        }
        test("divide BigInteger") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigInteger.ONE
            value / rhs
            verify { value.divide(rhs) }
        }
        test("divide BigFraction") {
            val value = mockk<BigFraction>(relaxed = true)
            val rhs = BigFraction.ONE
            value / rhs
            verify { value.divide(rhs) }
        }
    }
}
