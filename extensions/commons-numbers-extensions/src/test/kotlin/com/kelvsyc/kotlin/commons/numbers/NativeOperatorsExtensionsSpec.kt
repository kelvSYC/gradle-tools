package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.core.NativeOperators

class NativeOperatorsExtensionsSpec : FunSpec() {
    interface Dummy : NativeOperators<Dummy>

    init {
        test("subtract") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = mockk<Dummy>()
            every { lhs.subtract(any()) } returns mockk() // workaround for generic mocking

            lhs - rhs
            verify {
                lhs.subtract(rhs)
            }
        }
        test("multiply") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = 1
            every { lhs.multiply(any<Int>()) } returns mockk() // workaround for generic mocking

            lhs * rhs
            verify {
                lhs.multiply(rhs)
            }
        }
        test("divide") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = mockk<Dummy>()
            every { lhs.divide(any()) } returns mockk() // workaround for generic mocking

            lhs / rhs
            verify {
                lhs.divide(rhs)
            }
        }
    }
}
