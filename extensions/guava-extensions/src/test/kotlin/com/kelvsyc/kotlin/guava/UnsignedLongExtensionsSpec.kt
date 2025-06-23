package com.kelvsyc.kotlin.guava

import com.google.common.primitives.UnsignedLong
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import io.mockk.mockk
import io.mockk.verify

class UnsignedLongExtensionsSpec : FunSpec() {
    init {
        test("div") {
            val value = mockk<UnsignedLong>(relaxed = true)
            val other = mockk<UnsignedLong>(relaxed = true)

            value / other

            verify {
                value.dividedBy(other)
            }
        }

        test("rem") {
            val value = mockk<UnsignedLong>(relaxed = true)
            val other = mockk<UnsignedLong>(relaxed = true)

            value % other

            verify {
                value.mod(other)
            }
        }

        test("asULong") {
            checkAll<UnsignedLong>(Arb.long().map(UnsignedLong::fromLongBits)) {
                val converted = it.asULong

                it.toLong() shouldBeEqual converted.toLong()
            }
        }

        test("asGuavaUnsignedLong") {
            checkAll<ULong> {
                val converted = it.asGuavaUnsignedLong

                it.toLong() shouldBeEqual converted.toLong()
            }
        }
    }
}
