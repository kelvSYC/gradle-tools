package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.arbitrary.uLong
import io.kotest.property.checkAll

@OptIn(ExperimentalUnsignedTypes::class)
class RotateRightSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            test("Normal") {
                checkAll<Byte, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("UByte") {
            val traits = TypeTraits.UByte
            test("Normal") {
                checkAll<UByte, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("Short") {
            val traits = TypeTraits.Short
            test("Normal") {
                checkAll<Short, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("UShort") {
            val traits = TypeTraits.UShort
            test("Normal") {
                checkAll<UShort, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("Int") {
            val traits = TypeTraits.Int
            test("Normal") {
                checkAll<Int, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("UInt") {
            val traits = TypeTraits.UInt
            test("Normal") {
                checkAll<UInt, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("Long") {
            val traits = TypeTraits.Long
            test("Normal") {
                checkAll<Long, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("ULong") {
            val traits = TypeTraits.ULong
            test("Normal") {
                checkAll<ULong, Int> { value, bitCount ->
                    traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("ByteArray") {
            val traits = TypeTraits.ByteArray
            test("Normal") {
                checkAll<Int, Int> { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value)

                    val result = traits.rotateRight(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("UByteArray") {
            val traits = TypeTraits.UByteArray
            test("Normal") {
                checkAll<Int, Int> { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value).asUByteArray()

                    val result = traits.rotateRight(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or (b.toInt() shl (index * UByte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("ShortArray") {
            val traits = TypeTraits.ShortArray
            test("Normal") {
                checkAll<Int, Int> { value, bitCount ->
                    val bytes = ShortArray(Int.SIZE_BITS / Short.SIZE_BITS) {
                        (value ushr (it * Short.SIZE_BITS)).toShort()
                    }

                    val result = traits.rotateRight(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or (b.toUShort().toInt() shl (index * Short.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("UShortArray") {
            val traits = TypeTraits.UShortArray
            test("Normal") {
                checkAll<Int, Int> { value, bitCount ->
                    val bytes = UShortArray(Int.SIZE_BITS / UShort.SIZE_BITS) {
                        (value ushr (it * UShort.SIZE_BITS)).toUShort()
                    }

                    val result = traits.rotateRight(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0) { index, acc, b ->
                        acc or (b.toInt() shl (index * UShort.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("IntArray") {
            val traits = TypeTraits.IntArray
            test("Normal") {
                checkAll<Long, Int> { value, bitCount ->
                    val bytes = IntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                        (value ushr (it * Int.SIZE_BITS)).toInt()
                    }

                    val result = traits.rotateRight(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0L) { index, acc, b ->
                        acc or (b.toUInt().toLong() shl (index * Int.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("UIntArray") {
            val traits = TypeTraits.UIntArray
            test("Normal") {
                checkAll<Long, Int> { value, bitCount ->
                    val bytes = UIntArray(Long.SIZE_BITS / UInt.SIZE_BITS) {
                        (value ushr (it * UInt.SIZE_BITS)).toUInt()
                    }

                    val result = traits.rotateRight(bytes, bitCount)
                    val rebuilt = result.foldIndexed(0L) { index, acc, b ->
                        acc or ((b.toLong()) shl (index * UInt.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual value.rotateRight(bitCount)
                }
            }
        }

        context("LongArray") {
            val traits = TypeTraits.LongArray
            val arb = Arb.bind(Arb.long(), Arb.long()) { a, b -> longArrayOf(a, b) }
            test("Normal") {
                checkAll(arb, Arb.nonNegativeInt()) { value, bitCount ->
                    val trueBitCount = bitCount.rem(value.size * Long.SIZE_BITS)
                    val (expectedHigh, expectedLow) = if (trueBitCount == 0) {
                        value[1] to value[0]
                    } else if (trueBitCount < Long.SIZE_BITS) {
                        val high = (value[1] ushr trueBitCount) or (value[0] shl (value.size * Long.SIZE_BITS - trueBitCount))
                        val low = (value[0] ushr trueBitCount) or (value[1] shl (value.size * Long.SIZE_BITS - trueBitCount))
                        high to low
                    } else if (trueBitCount == Long.SIZE_BITS) {
                        // We rotate by exactly a full element
                        value[0] to value[1]
                    } else {
                        // We rotate by more than a full element, so the bytes are effectively "swapped"
                        val adjustedBitCount = trueBitCount - Long.SIZE_BITS
                        val high = (value[0] ushr adjustedBitCount) or (value[1] shl (value.size * Long.SIZE_BITS - adjustedBitCount))
                        val low = (value[1] ushr adjustedBitCount) or (value[0] shl (value.size * Long.SIZE_BITS - adjustedBitCount))
                        high to low
                    }

                    val result = traits.rotateRight(value, bitCount)

                    result.shouldHaveSize(2)
                    result[1] shouldBeEqual expectedHigh
                    result[0] shouldBeEqual expectedLow
                }
            }
        }

        context("ULongArray") {
            val traits = TypeTraits.ULongArray
            val arb = Arb.bind(Arb.uLong(), Arb.uLong()) { a, b -> ulongArrayOf(a, b) }
            test("Normal") {
                checkAll(arb, Arb.nonNegativeInt()) { value, bitCount ->
                    val trueBitCount = bitCount.rem(value.size * ULong.SIZE_BITS)
                    val (expectedHigh, expectedLow) = if (trueBitCount == 0) {
                        value[1] to value[0]
                    } else if (trueBitCount < ULong.SIZE_BITS) {
                        val high =
                            (value[1] shr trueBitCount) or (value[0] shl (value.size * ULong.SIZE_BITS - trueBitCount))
                        val low =
                            (value[0] shr trueBitCount) or (value[1] shl (value.size * ULong.SIZE_BITS - trueBitCount))
                        high to low
                    } else if (trueBitCount == ULong.SIZE_BITS) {
                        // We rotate by exactly a full element
                        value[0] to value[1]
                    } else {
                        // We rotate by more than a full element, so the bytes are effectively "swapped"
                        val adjustedBitCount = trueBitCount - Long.SIZE_BITS
                        val high = (value[0] shr adjustedBitCount) or (value[1] shl (value.size * ULong.SIZE_BITS - adjustedBitCount))
                        val low = (value[1] shr adjustedBitCount) or (value[0] shl (value.size * ULong.SIZE_BITS - adjustedBitCount))
                        high to low
                    }

                    val result = traits.rotateRight(value, bitCount)

                    result.shouldHaveSize(2)
                    result[1] shouldBeEqual expectedHigh
                    result[0] shouldBeEqual expectedLow
                }
            }
        }
    }
}
