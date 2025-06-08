package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.core.DD

class DdExtensionsSpec : FunSpec() {
    init {
        test("negate") {
            val value = mockk<DD>(relaxed = true)
            -value
            verify { value.negate() }
        }

        test("add double") {
            val value = mockk<DD>(relaxed = true)
            val rhs = 0.0
            value + rhs
            verify { value.add(rhs) }
        }
        test("add DD") {
            val value = mockk<DD>(relaxed = true)
            val rhs = DD.ZERO
            value + rhs
            verify { value.add(rhs) }
        }
        test("subtract double") {
            val value = mockk<DD>(relaxed = true)
            val rhs = 0.0
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("subtract DD") {
            val value = mockk<DD>(relaxed = true)
            val rhs = DD.ZERO
            value - rhs
            verify { value.subtract(rhs) }
        }
        test("multiply Int") {
            val value = mockk<DD>(relaxed = true)
            val rhs = 1
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply double") {
            val value = mockk<DD>(relaxed = true)
            val rhs = 0.0
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("multiply DD") {
            val value = mockk<DD>(relaxed = true)
            val rhs = DD.ZERO
            value * rhs
            verify { value.multiply(rhs) }
        }
        test("divide double") {
            val value = mockk<DD>(relaxed = true)
            val rhs = 1.0
            value / rhs
            verify { value.divide(rhs) }
        }
        test("divide DD") {
            val value = mockk<DD>(relaxed = true)
            val rhs = DD.ONE
            value / rhs
            verify { value.divide(rhs) }
        }
    }
}
