package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

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
    }
}
