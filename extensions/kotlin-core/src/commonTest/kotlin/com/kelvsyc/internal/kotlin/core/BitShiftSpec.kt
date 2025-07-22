package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class BitShiftSpec : FunSpec() {
    init {
        test("Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value.toInt() shl bitCount).toByte()
            }
            checkAll<Byte, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value.toUByte().toInt() ushr bitCount).toByte()
            }
            checkAll<Byte, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value.toInt() shr bitCount).toByte()
            }
        }

        test("UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value.toInt() shl bitCount).toUByte()
            }
            checkAll<UByte, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value.toInt() ushr bitCount).toUByte()
            }
            checkAll<UByte, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value.toByte().toInt() shr bitCount).toUByte()
            }
        }

        test("Short") {
            val traits = TypeTraits.Short
            checkAll<Short, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value.toInt() shl bitCount).toShort()
            }
            checkAll<Short, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value.toUShort().toInt() ushr bitCount).toShort()
            }
            checkAll<Short, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value.toInt() shr bitCount).toShort()
            }
        }

        test("UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value.toInt() shl bitCount).toUShort()
            }
            checkAll<UShort, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value.toInt() ushr bitCount).toUShort()
            }
            checkAll<UShort, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value.toShort().toInt() shr bitCount).toUShort()
            }
        }

        test("Int") {
            val traits = TypeTraits.Int
            checkAll<Int, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value shl bitCount)
            }
            checkAll<Int, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value ushr bitCount)
            }
            checkAll<Int, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value shr bitCount)
            }
        }

        test("UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value shl bitCount)
            }
            checkAll<UInt, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value shr bitCount)
            }
            checkAll<UInt, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value.toInt() shr bitCount).toUInt()
            }
        }

        test("Long") {
            val traits = TypeTraits.Long
            checkAll<Long, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value shl bitCount)
            }
            checkAll<Long, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value ushr bitCount)
            }
            checkAll<Long, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value shr bitCount)
            }
        }

        test("ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong, Int> { value, bitCount ->
                traits.leftShift(value, bitCount) shouldBeEqual (value shl bitCount)
            }
            checkAll<ULong, Int> { value, bitCount ->
                traits.rightShift(value, bitCount) shouldBeEqual (value shr bitCount)
            }
            checkAll<ULong, Int> { value, bitCount ->
                traits.arithmeticRightShift(value, bitCount) shouldBeEqual (value.toLong() shr bitCount).toULong()
            }
        }
    }
}
