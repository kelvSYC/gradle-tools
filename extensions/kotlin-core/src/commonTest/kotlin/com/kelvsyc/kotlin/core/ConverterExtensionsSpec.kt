package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence

class ConverterExtensionsSpec : FunSpec() {
    init {
        test("wrap Unary") {
            val forward = mockk<(Any) -> Any>(relaxed = true)
            val reverse = mockk<(Any) -> Any>(relaxed = true)
            val converter = Converter.of(forward, reverse)
            val op = mockk<(Any) -> Any>(relaxed = true)
            val arg = mockk<Any>()
            val reversed = mockk<Any>()
            val rawResult = mockk<Any>()
            every { reverse(arg) } returns reversed
            every { op(reversed) } returns rawResult

            val wrapped = converter.wrap(op)
            wrapped(arg)

            verifySequence {
                reverse(arg)
                op(reversed)
                forward(rawResult)
            }
        }

        test("wrap Binary") {
            val forward = mockk<(Any) -> Any>(relaxed = true)
            val reverse = mockk<(Any) -> Any>(relaxed = true)
            val converter = Converter.of(forward, reverse)
            val op = mockk<(Any, Any) -> Any>(relaxed = true)
            val lhs = mockk<Any>()
            val reversedLhs = mockk<Any>()
            every { reverse(lhs) } returns reversedLhs
            val rhs = mockk<Any>()
            val reversedRhs = mockk<Any>()
            every { reverse(rhs) } returns reversedRhs
            val rawResult = mockk<Any>()
            every { op(reversedLhs, reversedRhs) } returns rawResult

            val wrapped = converter.wrap(op)
            wrapped(lhs, rhs)

            verifySequence {
                reverse(lhs)
                reverse(rhs)
                op(reversedLhs, reversedRhs)
                forward(rawResult)
            }
        }

        test("andThen") {
            val forward1 = mockk<(Any) -> Any>(relaxed = true)
            val reverse1 = mockk<(Any) -> Any>(relaxed = true)
            val forward2 = mockk<(Any) -> Any>(relaxed = true)
            val reverse2 = mockk<(Any) -> Any>(relaxed = true)
            val converter1 = Converter.of(forward1, reverse1)
            val converter2 = Converter.of(forward2, reverse2)
            val converter = converter1.andThen(converter2)
            val arg = mockk<Any>()
            val intermediate = mockk<Any>()
            every { forward1(arg) } returns intermediate
            val arg2 = mockk<Any>()
            val intermediate2 = mockk<Any>()
            every { reverse2(arg2) } returns intermediate2

            converter(arg)
            converter.reverse(arg2)

            verifySequence {
                forward1(arg)
                forward2(intermediate)
            }
            verifySequence {
                reverse2(arg2)
                reverse1(intermediate2)
            }
        }
    }
}
