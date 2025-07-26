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
import kotlin.experimental.and

class RoundingRightShiftSpec : FunSpec() {
    init {
        test("Byte") {
            val traits = TypeTraits.Byte
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.byte(), bitCountArb) { value, bitCount ->
                val base = (value.toUByte().toInt() ushr bitCount).toByte()
                val baseIsOdd = (base and 1).toInt() != 0
                val mask = (if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1).toByte()
                val half = (1 shl (bitCount - 1)).toUByte()
                val remainder = (value and mask).toUByte()

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half.toInt() != 0) {
                        result shouldBeEqual (base + 1).toByte()
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1).toByte()
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("UByte") {
            val traits = TypeTraits.UByte
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uByte(), bitCountArb) { value, bitCount ->
                val base = (value.toInt() ushr bitCount).toUByte()
                val baseIsOdd = (base and 1U).toInt() != 0
                val mask = (if (bitCount >= traits.sizeBits) 0U.inv() else (1U shl bitCount) - 1U).toUByte()
                val half = (1U shl (bitCount - 1)).toUByte()
                val remainder = value and mask

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half.toInt() != 0) {
                        result shouldBeEqual (base + 1U).toUByte()
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1U).toUByte()
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("Short") {
            val traits = TypeTraits.Short
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.short(), bitCountArb) { value, bitCount ->
                val base = (value.toUShort().toInt() ushr bitCount).toShort()
                val baseIsOdd = (base and 1).toInt() != 0
                val mask = (if (bitCount >= traits.sizeBits) 0.inv() else (1 shl bitCount) - 1).toShort()
                val half = (1 shl (bitCount - 1)).toUShort()
                val remainder = (value and mask).toUShort()

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half.toInt() != 0) {
                        result shouldBeEqual (base + 1).toShort()
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1).toShort()
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("UShort") {
            val traits = TypeTraits.UShort
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uShort(), bitCountArb) { value, bitCount ->
                val base = (value.toInt() ushr bitCount).toUShort()
                val baseIsOdd = (base and 1U).toInt() != 0
                val mask = (if (bitCount >= traits.sizeBits) 0U.inv() else (1U shl bitCount) - 1U).toUShort()
                val half = (1U shl (bitCount - 1)).toUShort()
                val remainder = value and mask

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half.toInt() != 0) {
                        result shouldBeEqual (base + 1U).toUShort()
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1U).toUShort()
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("Int") {
            val traits = TypeTraits.Int
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.int(), bitCountArb) { value, bitCount ->
                val base = if (bitCount >= traits.sizeBits) 0 else (value ushr bitCount)
                val baseIsOdd = base and 1 != 0
                val mask = if (bitCount >= traits.sizeBits) 0.inv() else ((1 shl bitCount) - 1)
                val half = 1U shl (bitCount - 1)
                val remainder = (value and mask).toUInt()

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half != 0U) {
                        result shouldBeEqual (base + 1)
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1)
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("UInt") {
            val traits = TypeTraits.UInt
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uInt(), bitCountArb) { value, bitCount ->
                val base = if (bitCount >= traits.sizeBits) 0U else (value shr bitCount)
                val baseIsOdd = base and 1U != 0U
                val mask = if (bitCount >= traits.sizeBits) 0U.inv() else ((1U shl bitCount) - 1U)
                val half = 1U shl (bitCount - 1)
                val remainder = value and mask

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half != 0U) {
                        result shouldBeEqual (base + 1U)
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1U)
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("Long") {
            val traits = TypeTraits.Long
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.long(), bitCountArb) { value, bitCount ->
                val base = if (bitCount >= traits.sizeBits) 0 else (value ushr bitCount)
                val baseIsOdd = base and 1L != 0L
                val mask = if (bitCount >= traits.sizeBits) 0L.inv() else ((1L shl bitCount) - 1L)
                val half = 1UL shl (bitCount - 1)
                val remainder = (value and mask).toULong()

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half != 0UL) {
                        result shouldBeEqual (base + 1L)
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1L)
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }

        test("ULong") {
            val traits = TypeTraits.ULong
            val bitCountArb = Arb.nonNegativeInt(traits.sizeBits)
            checkAll(Arb.uLong(), bitCountArb) { value, bitCount ->
                val base = if (bitCount >= traits.sizeBits) 0U else (value shr bitCount)
                val baseIsOdd = base and 1UL != 0UL
                val mask = if (bitCount >= traits.sizeBits) 0UL.inv() else ((1UL shl bitCount) - 1UL)
                val half = 1UL shl (bitCount - 1)
                val remainder = value and mask

                val result = traits.roundingRightShift(value, bitCount)

                if (baseIsOdd) {
                    if (remainder >= half && half != 0UL) {
                        result shouldBeEqual (base + 1UL)
                    } else {
                        result shouldBeEqual base
                    }
                } else {
                    if (remainder > half) {
                        result shouldBeEqual (base + 1UL)
                    } else {
                        result shouldBeEqual base
                    }
                }
            }
        }
    }
}
