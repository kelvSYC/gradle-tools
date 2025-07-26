package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.uByte
import io.kotest.property.arbitrary.uInt
import io.kotest.property.arbitrary.uLong
import io.kotest.property.arbitrary.uShort
import io.kotest.property.checkAll

class LeftShiftSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            val valueArb = Arb.byte()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toByte()
                    } else {
                        result shouldBeEqual (value.toInt() shl bitCount).toByte()
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("UByte") {
            val traits = TypeTraits.UByte
            val valueArb = Arb.uByte()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toUByte()
                    } else {
                        result shouldBeEqual (value.toInt() shl bitCount).toUByte()
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("Short") {
            val traits = TypeTraits.Short
            val valueArb = Arb.short()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toShort()
                    } else {
                        result shouldBeEqual (value.toInt() shl bitCount).toShort()
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("UShort") {
            val traits = TypeTraits.UShort
            val valueArb = Arb.uShort()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toUShort()
                    } else {
                        result shouldBeEqual (value.toInt() shl bitCount).toUShort()
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("Int") {
            val traits = TypeTraits.Int
            val valueArb = Arb.int()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0
                    } else {
                        result shouldBeEqual (value shl bitCount)
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("UInt") {
            val traits = TypeTraits.UInt
            val valueArb = Arb.uInt()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0U
                    } else {
                        result shouldBeEqual (value shl bitCount)
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("Long") {
            val traits = TypeTraits.Long
            val valueArb = Arb.long()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0L
                    } else {
                        result shouldBeEqual (value shl bitCount)
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }

        context("ULong") {
            val traits = TypeTraits.ULong
            val valueArb = Arb.uLong()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.leftShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0UL
                    } else {
                        result shouldBeEqual (value shl bitCount)
                    }
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.leftShift(value, bitCount)
                    }
                }
            }
        }
    }
}
