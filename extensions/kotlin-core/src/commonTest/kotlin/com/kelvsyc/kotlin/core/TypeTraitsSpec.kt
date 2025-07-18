package com.kelvsyc.kotlin.core

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.math.absoluteValue

@OptIn(ExperimentalStdlibApi::class)
class TypeTraitsSpec : FunSpec() {
    init {
        test("BitCollection Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<Byte> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Byte.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<Byte> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Byte> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<UByte> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< UByte.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<UByte> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<UByte> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection Short") {
            val traits = TypeTraits.Short
            checkAll<Short> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<Short> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Short.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<Short> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Short> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it.toInt() and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<UShort> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< UShort.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it.toInt() and (1 shl i) != 0)
                }
            }
            checkAll<UShort> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<UShort> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection Int") {
            val traits = TypeTraits.Int
            checkAll<Int> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1 shl index) != 0) shouldBeEqual bit
                }
            }
            checkAll<Int> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Int.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1 shl i) != 0)
                }
            }
            checkAll<Int> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Int> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1U shl index) != 0U) shouldBeEqual bit
                }
            }
            checkAll<UInt> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< UInt.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1U shl i) != 0U)
                }
            }
            checkAll<UInt> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<UInt> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection Long") {
            val traits = TypeTraits.Long
            checkAll<Long> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1L shl index) != 0L) shouldBeEqual bit
                }
            }
            checkAll<Long> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< Long.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1L shl i) != 0L)
                }
            }
            checkAll<Long> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<Long> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("BitCollection ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong> {
                val sequence = traits.asBitSequence(it)
                sequence.forEachIndexed { index, bit ->
                    (it and (1UL shl index) != 0UL) shouldBeEqual bit
                }
            }
            checkAll<ULong> {
                val set = traits.getSetBits(it)
                for (i in 0 ..< ULong.SIZE_BITS) {
                    set.contains(i) shouldBeEqual (it and (1UL shl i) != 0UL)
                }
            }
            checkAll<ULong> {
                traits.countLeadingZeroBits(it) shouldBeEqual it.countLeadingZeroBits()
            }
            checkAll<ULong> {
                traits.countTrailingZeroBits(it) shouldBeEqual it.countTrailingZeroBits()
            }
        }

        test("Addition Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs).toByte()
            }
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs).toByte()
            }
        }

        test("Addition UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs).toUByte()
            }
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs).toUByte()
            }
        }

        test("Addition Short") {
            val traits = TypeTraits.Short
            checkAll<Short, Short> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs).toShort()
            }
            checkAll<Short, Short> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs).toShort()
            }
        }

        test("Addition UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs).toUShort()
            }
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs).toUShort()
            }
        }

        test("Addition Int") {
            val traits = TypeTraits.Int
            checkAll<Int, Int> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<Int, Int> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Addition UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Addition Long") {
            val traits = TypeTraits.Long
            checkAll<Long, Long> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<Long, Long> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Addition ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Addition Float") {
            val traits = TypeTraits.Float
            checkAll<Float, Float> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<Float, Float> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Addition Double") {
            val traits = TypeTraits.Double
            checkAll<Double, Double> { lhs, rhs ->
                traits.add(lhs, rhs) shouldBeEqual (lhs + rhs)
            }
            checkAll<Double, Double> { lhs, rhs ->
                traits.subtract(lhs, rhs) shouldBeEqual (lhs - rhs)
            }
        }

        test("Multiplication Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte, Byte> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs).toByte()
            }
            checkAll<Byte, Byte> { lhs, rhs ->
                if (rhs == 0.toByte()) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs).toByte()
                }
            }
        }

        test("Multiplication UByte") {
            val traits = TypeTraits.UByte
            checkAll<UByte, UByte> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs).toUByte()
            }
            checkAll<UByte, UByte> { lhs, rhs ->
                if (rhs == 0.toUByte()) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs).toUByte()
                }
            }
        }

        test("Multiplication Short") {
            val traits = TypeTraits.Short
            checkAll<Short, Short> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs).toShort()
            }
            checkAll<Short, Short> { lhs, rhs ->
                if (rhs == 0.toShort()) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs).toShort()
                }
            }
        }

        test("Multiplication UShort") {
            val traits = TypeTraits.UShort
            checkAll<UShort, UShort> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs).toUShort()
            }
            checkAll<UShort, UShort> { lhs, rhs ->
                if (rhs == 0.toUShort()) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs).toUShort()
                }
            }
        }

        test("Multiplication Int") {
            val traits = TypeTraits.Int
            checkAll<Int, Int> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<Int, Int> { lhs, rhs ->
                if (rhs == 0) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
                }
            }
        }

        test("Multiplication UInt") {
            val traits = TypeTraits.UInt
            checkAll<UInt, UInt> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<UInt, UInt> { lhs, rhs ->
                if (rhs == 0U) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
                }
            }
        }

        test("Multiplication Long") {
            val traits = TypeTraits.Long
            checkAll<Long, Long> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<Long, Long> { lhs, rhs ->
                if (rhs == 0L) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
                }
            }
        }

        test("Multiplication ULong") {
            val traits = TypeTraits.ULong
            checkAll<ULong, ULong> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<ULong, ULong> { lhs, rhs ->
                if (rhs == 0UL) {
                    shouldThrowExactly<ArithmeticException> {
                        traits.divide(lhs, rhs)
                    }
                } else {
                    traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
                }
            }
        }

        test("Multiplication Float") {
            val traits = TypeTraits.Float
            checkAll<Float, Float> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<Float, Float> { lhs, rhs ->
                traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
            }
        }

        test("Multiplication Double") {
            val traits = TypeTraits.Double
            checkAll<Double, Double> { lhs, rhs ->
                traits.multiply(lhs, rhs) shouldBeEqual (lhs * rhs)
            }
            checkAll<Double, Double> { lhs, rhs ->
                traits.divide(lhs, rhs) shouldBeEqual (lhs / rhs)
            }
        }

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

        test("Signed Byte") {
            val traits = TypeTraits.Byte
            checkAll<Byte> {
                traits.isPositive(it) shouldBeEqual (it > 0)
            }
            checkAll<Byte> {
                traits.isNegative(it) shouldBeEqual (it < 0)
            }
            checkAll<Byte> {
                traits.negate(it) shouldBeEqual (-it).toByte()
            }
            checkAll<Byte> {
                traits.absoluteValue(it) shouldBeEqual it.toInt().absoluteValue.toByte()
            }
        }

        test("Signed Short") {
            val traits = TypeTraits.Short
            checkAll<Short> {
                traits.isPositive(it) shouldBeEqual (it > 0)
            }
            checkAll<Short> {
                traits.isNegative(it) shouldBeEqual (it < 0)
            }
            checkAll<Short> {
                traits.negate(it) shouldBeEqual (-it).toShort()
            }
            checkAll<Short> {
                traits.absoluteValue(it) shouldBeEqual it.toInt().absoluteValue.toShort()
            }
        }

        test("Signed Int") {
            val traits = TypeTraits.Int
            checkAll<Int> {
                traits.isPositive(it) shouldBeEqual (it > 0)
            }
            checkAll<Int> {
                traits.isNegative(it) shouldBeEqual (it < 0)
            }
            checkAll<Int> {
                traits.negate(it) shouldBeEqual -it
            }
            checkAll<Int> {
                traits.absoluteValue(it) shouldBeEqual it.absoluteValue
            }
        }

        test("Signed Long") {
            val traits = TypeTraits.Long
            checkAll<Long> {
                traits.isPositive(it) shouldBeEqual (it > 0)
            }
            checkAll<Long> {
                traits.isNegative(it) shouldBeEqual (it < 0)
            }
            checkAll<Long> {
                traits.negate(it) shouldBeEqual -it
            }
            checkAll<Long> {
                traits.absoluteValue(it) shouldBeEqual it.absoluteValue
            }
        }

        test("Signed Float") {
            val traits = TypeTraits.Float
            checkAll<Float> {
                traits.isPositive(it) shouldBeEqual (it > 0)
            }
            checkAll<Float> {
                traits.isNegative(it) shouldBeEqual (it < 0)
            }
            checkAll<Float> {
                traits.negate(it) shouldBeEqual -it
            }
            checkAll<Float> {
                traits.absoluteValue(it) shouldBeEqual it.absoluteValue
            }
        }

        test("Signed Double") {
            val traits = TypeTraits.Double
            checkAll<Double> {
                traits.isPositive(it) shouldBeEqual (it > 0)
            }
            checkAll<Double> {
                traits.isNegative(it) shouldBeEqual (it < 0)
            }
            checkAll<Double> {
                traits.negate(it) shouldBeEqual -it
            }
            checkAll<Double> {
                traits.absoluteValue(it) shouldBeEqual it.absoluteValue
            }
        }
    }
}
