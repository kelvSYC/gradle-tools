package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class RotateLeftSpec : FunSpec() {
    init {
        context("Byte") {
            val traits = TypeTraits.Byte
            test("Normal") {
                checkAll<Byte, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("UByte") {
            val traits = TypeTraits.UByte
            test("Normal") {
                checkAll<UByte, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("Short") {
            val traits = TypeTraits.Short
            test("Normal") {
                checkAll<Short, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("UShort") {
            val traits = TypeTraits.UShort
            test("Normal") {
                checkAll<UShort, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("Int") {
            val traits = TypeTraits.Int
            test("Normal") {
                checkAll<Int, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("UInt") {
            val traits = TypeTraits.UInt
            test("Normal") {
                checkAll<UInt, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("Long") {
            val traits = TypeTraits.Long
            test("Normal") {
                checkAll<Long, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }

        context("ULong") {
            val traits = TypeTraits.ULong
            test("Normal") {
                checkAll<ULong, Int> { value, bitCount ->
                    traits.rotateLeft(value, bitCount) shouldBeEqual value.rotateLeft(bitCount)
                }
            }
        }
    }
}
