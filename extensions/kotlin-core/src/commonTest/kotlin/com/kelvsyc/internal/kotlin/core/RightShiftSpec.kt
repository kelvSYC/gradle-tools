package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
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

@OptIn(ExperimentalUnsignedTypes::class)
class RightShiftSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            val valueArb = Arb.byte()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toByte()
                    } else {
                        result shouldBeEqual (value.toUByte().toInt() ushr bitCount).toByte()
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toUByte()
                    } else {
                        result shouldBeEqual (value.toInt() ushr bitCount).toUByte()
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toShort()
                    } else {
                        result shouldBeEqual (value.toUShort().toInt() ushr bitCount).toShort()
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0.toUShort()
                    } else {
                        result shouldBeEqual (value.toInt() ushr bitCount).toUShort()
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0
                    } else {
                        result shouldBeEqual (value ushr bitCount)
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0U
                    } else {
                        result shouldBeEqual (value shr bitCount)
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0L
                    } else {
                        result shouldBeEqual (value ushr bitCount)
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
                    val result = traits.rightShift(value, bitCount)

                    if (bitCount >= traits.sizeBits) {
                        result shouldBeEqual 0UL
                    } else {
                        result shouldBeEqual (value shr bitCount)
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

        context("ByteArray") {
            val baseValueArb = Arb.int()
            val traits = TypeTraits.ByteArray
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Int.SIZE_BITS)
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value)

                    val result = traits.rightShift(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual TypeTraits.Int.rightShift(value, bitCount)
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        val bytes = TypeTraits.Int.asByteArray(value)
                        traits.rightShift(bytes, bitCount)
                    }
                }
            }
        }

        context("UByteArray") {
            val baseValueArb = Arb.int()
            val traits = TypeTraits.UByteArray
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Int.SIZE_BITS)
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value).toUByteArray()

                    val result = traits.rightShift(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or ((b.toInt() and 0xFF) shl (index * UByte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual TypeTraits.Int.rightShift(value, bitCount)
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        val bytes = TypeTraits.Int.asByteArray(value).toUByteArray()
                        traits.rightShift(bytes, bitCount)
                    }
                }
            }
        }

        context("ShortArray") {
            val baseValueArb = Arb.int()
            val traits = TypeTraits.ShortArray
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Int.SIZE_BITS)
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    val bytes = ShortArray(Int.SIZE_BITS / Short.SIZE_BITS) {
                        (value ushr (it * Short.SIZE_BITS)).toShort()
                    }

                    val result = traits.rightShift(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or ((b.toInt() and 0xFFFF) shl (index * Short.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual TypeTraits.Int.rightShift(value, bitCount)
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        val bytes = ShortArray(Int.SIZE_BITS / Short.SIZE_BITS) {
                            (value ushr (it * Short.SIZE_BITS)).toShort()
                        }
                        traits.rightShift(bytes, bitCount)
                    }
                }
            }
        }

        context("UShortArray") {
            val baseValueArb = Arb.int()
            val traits = TypeTraits.UShortArray
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Int.SIZE_BITS)
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    val bytes = UShortArray(Int.SIZE_BITS / UShort.SIZE_BITS) {
                        (value ushr (it * UShort.SIZE_BITS)).toUShort()
                    }

                    val result = traits.rightShift(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or ((b.toInt() and 0xFFFF) shl (index * UShort.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual TypeTraits.Int.rightShift(value, bitCount)
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        val bytes = UShortArray(Int.SIZE_BITS / UShort.SIZE_BITS) {
                            (value ushr (it * UShort.SIZE_BITS)).toUShort()
                        }
                        traits.rightShift(bytes, bitCount)
                    }
                }
            }

        }

        context("IntArray") {
            val baseValueArb = Arb.long()
            val traits = TypeTraits.IntArray
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Long.SIZE_BITS)
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    val bytes = IntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                        (value ushr (it * Int.SIZE_BITS)).toInt()
                    }

                    val result = traits.rightShift(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0L) { index, acc, b ->
                        acc or (b.toUInt().toLong() shl (index * Int.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual TypeTraits.Long.rightShift(value, bitCount)
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        val bytes = IntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                            (value ushr (it * Int.SIZE_BITS)).toInt()
                        }
                        traits.rightShift(bytes, bitCount)
                    }
                }
            }
        }

        context("UIntArray") {
            val baseValueArb = Arb.long()
            val traits = TypeTraits.UIntArray
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Int.SIZE_BITS)
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    val bytes = UIntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                        (value ushr (it * UInt.SIZE_BITS)).toUInt()
                    }

                    val result = traits.rightShift(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0L) { index, acc, b ->
                        acc or ((b.toLong()) shl (index * UInt.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual TypeTraits.Long.rightShift(value, bitCount)
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(baseValueArb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        val bytes = UIntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                            (value ushr (it * UShort.SIZE_BITS)).toUInt()
                        }
                        traits.rightShift(bytes, bitCount)
                    }
                }
            }
        }

        context("LongArray") {
            val traits = TypeTraits.LongArray
            val arb = Arb.bind(Arb.long(), Arb.long()) { a, b -> longArrayOf(a, b) }
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(Long.SIZE_BITS * 2)
                checkAll(arb, bitCountArb) { value, bitCount ->
                    val (expectedHigh, expectedLow) = if (bitCount == 0) {
                        value[1] to value[0]
                    } else if (bitCount < Long.SIZE_BITS) {
                        (value[1] ushr bitCount) to ((value[1] shl (Long.SIZE_BITS - bitCount)) or (value[0] ushr bitCount))
                    } else if (bitCount < Long.SIZE_BITS * 2) {
                        0L to (value[1] ushr (bitCount - Long.SIZE_BITS))
                    } else {
                        0L to 0L
                    }

                    val result = traits.rightShift(value, bitCount)

                    result.shouldHaveSize(2)
                    result[1] shouldBeEqual expectedHigh
                    result[0] shouldBeEqual expectedLow
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(arb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.rightShift(value, bitCount)
                    }
                }
            }
        }

        context("ULongArray") {
            val traits = TypeTraits.ULongArray
            val arb = Arb.bind(Arb.uLong(), Arb.uLong()) { a, b -> ulongArrayOf(a, b) }
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(ULong.SIZE_BITS * 2)
                checkAll(arb, bitCountArb) { value, bitCount ->
                    val (expectedHigh, expectedLow) = if (bitCount == 0) {
                        value[1] to value[0]
                    } else if (bitCount < ULong.SIZE_BITS) {
                        (value[1] shr bitCount) to ((value[1] shl (ULong.SIZE_BITS - bitCount)) or (value[0] shr bitCount))
                    } else if (bitCount < ULong.SIZE_BITS * 2) {
                        0UL to (value[1] shr (bitCount - ULong.SIZE_BITS))
                    } else {
                        0UL to 0UL
                    }

                    val result = traits.rightShift(value, bitCount)

                    result.shouldHaveSize(2)
                    result[1] shouldBeEqual expectedHigh
                    result[0] shouldBeEqual expectedLow
                }
            }
            test("Negative Shift") {
                val bitCountArb = Arb.negativeInt()
                checkAll(arb, bitCountArb) { value, bitCount ->
                    shouldThrow<IllegalArgumentException> {
                        traits.rightShift(value, bitCount)
                    }
                }
            }
        }
    }
}
