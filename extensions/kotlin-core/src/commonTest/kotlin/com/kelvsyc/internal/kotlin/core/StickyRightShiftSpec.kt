package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
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
import kotlin.experimental.and
import kotlin.experimental.inv

class StickyRightShiftSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            val valueArb = Arb.byte()
            test("Normal") {
                val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
                checkAll(valueArb, bitCountArb) { value, bitCount ->
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = (if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1).toByte()

                    val plainShifted = (value.toUByte().toInt() ushr bitCount).toByte()
                    if ((value and mask).toInt() == 0) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1.toByte().inv()) shouldBeEqual (plainShifted and 1.toByte().inv())
                        (result and 1) shouldNotBeEqual 0
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = (if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1).toUByte()

                    val plainShifted = (value.toInt() ushr bitCount).toUByte()
                    if ((value and mask).toInt() == 0) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1.toUByte().inv()) shouldBeEqual (plainShifted and 1U.toUByte().inv())
                        (result and 1U) shouldNotBeEqual 0U
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = (if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1).toShort()

                    val plainShifted = (value.toUShort().toInt() ushr bitCount).toShort()
                    if ((value and mask).toInt() == 0) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1.toShort().inv()) shouldBeEqual (plainShifted and 1.toShort().inv())
                        (result and 1) shouldNotBeEqual 0
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = (if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1).toUShort()

                    val plainShifted = (value.toInt() ushr bitCount).toUShort()
                    if ((value and mask).toInt() == 0) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1.toUShort().inv()) shouldBeEqual (plainShifted and 1U.toUShort().inv())
                        (result and 1U) shouldNotBeEqual 0U
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1

                    val plainShifted = if (bitCount >= traits.sizeBits) 0 else value ushr bitCount
                    if (value and mask == 0) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1.inv()) shouldBeEqual (plainShifted and 1.inv())
                        (result and 1) shouldNotBeEqual 0
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = if (bitCount >= traits.sizeBits) 0U.inv() else (1U shl bitCount) - 1U

                    val plainShifted = if (bitCount >= traits.sizeBits) 0U else value shr bitCount
                    if (value and mask == 0U) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1U.inv()) shouldBeEqual (plainShifted and 1U.inv())
                        (result and 1U) shouldNotBeEqual 0U
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = if (bitCount >= traits.sizeBits) 0L.inv() else (1L shl bitCount) - 1L

                    val plainShifted = if (bitCount >= traits.sizeBits) 0L else value ushr bitCount
                    if (value and mask == 0L) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1L.inv()) shouldBeEqual (plainShifted and 1L.inv())
                        (result and 1L) shouldNotBeEqual 0L
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
                    val result = traits.stickyRightShift(value, bitCount)
                    val mask = if (bitCount >= traits.sizeBits) 0UL.inv() else (1UL shl bitCount) - 1UL

                    val plainShifted = if (bitCount >= traits.sizeBits) 0U else value shr bitCount
                    if (value and mask == 0UL) {
                        result shouldBeEqual plainShifted
                    } else {
                        (result and 1UL.inv()) shouldBeEqual (plainShifted and 1UL.inv())
                        (result and 1UL) shouldNotBeEqual 0UL
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
