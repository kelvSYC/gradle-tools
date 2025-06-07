package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.core.Addition

class AdditionExtensionsSpec : FunSpec() {
    interface Dummy : Addition<Dummy>

    init {
        test("unary negate") {
            val value = mockk<Dummy>(relaxed = true)
            every { value.negate() } returns mockk() // workaround for generic mocking

            -value
            verify {
                value.negate()
            }
        }
        test("plus") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = mockk<Dummy>()
            every { lhs.add(any()) } returns mockk() // workaround for generic mocking

            lhs + rhs
            verify {
                lhs.add(rhs)
            }
        }
        test("minus") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = mockk<Dummy>(relaxed = true)
            val rhsNegated = mockk<Dummy>()
            every { rhs.negate() } returns rhsNegated
            every { lhs.add(any()) } returns mockk() // workaround for generic mocking

            lhs - rhs
            verify {
                rhs.negate()
            }
            verify {
                lhs.add(rhsNegated)
            }
        }
    }
}
