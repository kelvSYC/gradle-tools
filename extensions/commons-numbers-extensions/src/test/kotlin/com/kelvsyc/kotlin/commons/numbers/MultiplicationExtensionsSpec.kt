package com.kelvsyc.kotlin.commons.numbers

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.numbers.core.Multiplication

class MultiplicationExtensionsSpec : FunSpec() {
    interface Dummy : Multiplication<Dummy>

    init {
        test("multiply") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = mockk<Dummy>()
            every { lhs.multiply(any()) } returns mockk() // workaround for generic mocking

            lhs * rhs
            verify {
                lhs.multiply(rhs)
            }
        }
        test("divide") {
            val lhs = mockk<Dummy>(relaxed = true)
            val rhs = mockk<Dummy>()
            val rhsReciproval = mockk<Dummy>()
            every { rhs.reciprocal() } returns rhsReciproval
            every { lhs.multiply(any()) } returns mockk() // workaround for generic mocking

            lhs / rhs
            verify {
                rhs.reciprocal()
            }
            verify {
                lhs.multiply(rhsReciproval)
            }
        }
    }
}
