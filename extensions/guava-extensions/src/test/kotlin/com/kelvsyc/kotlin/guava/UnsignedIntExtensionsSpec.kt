package com.kelvsyc.kotlin.guava

import com.google.common.primitives.UnsignedInteger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import io.mockk.mockk
import io.mockk.verify

class UnsignedIntExtensionsSpec : FunSpec() {
    init {
        test("div") {
            val value = mockk<UnsignedInteger>(relaxed = true)
            val other = mockk<UnsignedInteger>(relaxed = true)

            value / other

            verify {
                value.dividedBy(other)
            }
        }

        test("rem") {
            val value = mockk<UnsignedInteger>(relaxed = true)
            val other = mockk<UnsignedInteger>(relaxed = true)

            value % other

            verify {
                value.mod(other)
            }
        }

        test("asUInt") {
            checkAll<UnsignedInteger>(Arb.int().map(UnsignedInteger::fromIntBits)) {
                val converted = it.asUInt

                it.toInt() shouldBeEqual converted.toInt()
            }
        }

        test("asGuavaUnsignedInt") {
            checkAll<UInt> {
                val converted = it.asGuavaUnsignedInteger

                it.toInt() shouldBeEqual converted.toInt()
            }
        }
    }
}
