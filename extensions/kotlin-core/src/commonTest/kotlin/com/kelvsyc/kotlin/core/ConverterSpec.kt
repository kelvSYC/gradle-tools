package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import io.mockk.verify

class ConverterSpec : FunSpec() {
    init {
        test("identity") {
            val item = mockk<Any>()
            val converter = Converter.identity<Any>()

            converter(item) shouldBeSameInstanceAs item
            converter.reverse(item) shouldBeSameInstanceAs item
        }

        test("identity reverse") {
            val identity = Converter.identity<Any>()

            identity.reverse shouldBeSameInstanceAs identity
        }

        test("function based - forward") {
            val forward = mockk<(Any) -> Any>(relaxed = true)
            val backward = mockk<(Any) -> Any>(relaxed = true)
            val converter = Converter.of(forward, backward)
            val item = mockk<Any>()

            converter(item)

            verify { forward(item) }
        }

        test("function based - backward") {
            val forward = mockk<(Any) -> Any>(relaxed = true)
            val backward = mockk<(Any) -> Any>(relaxed = true)
            val converter = Converter.of(forward, backward)
            val item = mockk<Any>()

            converter.reverse(item)

            verify { backward(item) }
        }
    }
}
