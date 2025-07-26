package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.uByte
import io.kotest.property.arbitrary.uInt
import io.kotest.property.arbitrary.uLong
import io.kotest.property.arbitrary.uShort
import io.kotest.property.checkAll

class LeftShiftSpec : FunSpec() {
    init {
        test("Byte") {
            val traits = TypeTraits.Byte
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.byte(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0.toByte()
                } else {
                    result shouldBeEqual (value.toInt() shl bitCount).toByte()
                }
            }
        }

        test("UByte") {
            val traits = TypeTraits.UByte
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uByte(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0.toUByte()
                } else {
                    result shouldBeEqual (value.toInt() shl bitCount).toUByte()
                }
            }
        }

        test("Short") {
            val traits = TypeTraits.Short
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.short(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0.toShort()
                } else {
                    result shouldBeEqual (value.toInt() shl bitCount).toShort()
                }
            }
        }

        test("UShort") {
            val traits = TypeTraits.UShort
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uShort(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0.toUShort()
                } else {
                    result shouldBeEqual (value.toInt() shl bitCount).toUShort()
                }
            }
        }

        test("Int") {
            val traits = TypeTraits.Int
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.int(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0
                } else {
                    result shouldBeEqual (value shl bitCount)
                }
            }
        }

        test("UInt") {
            val traits = TypeTraits.UInt
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uInt(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0U
                } else {
                    result shouldBeEqual (value shl bitCount)
                }
            }
        }

        test("Long") {
            val traits = TypeTraits.Long
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.long(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0L
                } else {
                    result shouldBeEqual (value shl bitCount)
                }
            }
        }

        test("ULong") {
            val traits = TypeTraits.ULong
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uLong(), bitCountArb) { value, bitCount ->
                val result = traits.leftShift(value, bitCount)

                if (bitCount >= traits.sizeBits) {
                    result shouldBeEqual 0UL
                } else {
                    result shouldBeEqual (value shl bitCount)
                }
            }
        }
    }
}
