package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

class TypeTraitsSpec : FunSpec() {
    init {
        test("Bitwise Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Byte> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<UByte> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise Short") {
            val traits = TypeTraits.Short
            checkAll<Short, Short> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Short, Short> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Short, Short> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Short> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<UShort> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise Int") {
            val traits = TypeTraits.Int
            checkAll<Int, Int> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Int, Int> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Int, Int> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Int> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<UInt> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise Long") {
            val traits = TypeTraits.Long
            checkAll<Long, Long> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<Long, Long> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<Long, Long> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<Long> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("Bitwise ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.and(lhs, rhs) shouldBeEqual (lhs and rhs)
            }
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.or(lhs, rhs) shouldBeEqual (lhs or rhs)
            }
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.xor(lhs, rhs) shouldBeEqual (lhs xor rhs)
            }
            checkAll<ULong> {
                traits.inv(it) shouldBeEqual it.inv()
            }
        }

        test("BitShift Byte") {
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

        test("BitShift UByte") {
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

        test("BitShift Short") {
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

        test("BitShift UShort") {
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

        test("BitShift Int") {
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

        test("BitShift UInt") {
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

        test("BitShift Long") {
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

        test("BitShift ULong") {
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

        test("BitRotate Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<Byte, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<UByte, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate Short") {
            val traits = TypeTraits.Short
            checkAll<Short, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<Short, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<UShort, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate Int") {
            val traits = TypeTraits.Int
            checkAll<Int, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<Int, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<UInt, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate Long") {
            val traits = TypeTraits.Long
            checkAll<Long, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<Long, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }

        test("BitRotate ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong, Int> { value, bitCount ->
                traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
            }
            checkAll<ULong, Int> { value, bitCount ->
                traits.rotateRight(value, bitCount) shouldBeEqual value.rotateRight(bitCount)
            }
        }
    }
}
