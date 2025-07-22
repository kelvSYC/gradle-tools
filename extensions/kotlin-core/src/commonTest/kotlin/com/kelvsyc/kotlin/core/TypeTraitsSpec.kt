package com.kelvsyc.kotlin.core

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import kotlin.math.absoluteValue

class TypeTraitsSpec : FunSpec() {
    init {
        test("FloatingPoint Float") {
            val traits = TypeTraits.Float
            checkAll<Float> {
                traits.isFinite(it) shouldBeEqual it.isFinite()
            }
            checkAll<Float> {
                traits.isInfinite(it) shouldBeEqual it.isInfinite()
            }
            checkAll<Float> {
                traits.isNaN(it) shouldBeEqual it.isNaN()
            }
        }

        test("FloatingPoint Double") {
            val traits = TypeTraits.Double
            checkAll<Double> {
                traits.isFinite(it) shouldBeEqual it.isFinite()
            }
            checkAll<Double> {
                traits.isInfinite(it) shouldBeEqual it.isInfinite()
            }
            checkAll<Double> {
                traits.isNaN(it) shouldBeEqual it.isNaN()
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
