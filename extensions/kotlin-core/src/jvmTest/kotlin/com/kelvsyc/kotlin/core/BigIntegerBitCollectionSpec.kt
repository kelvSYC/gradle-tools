package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class BigIntegerBitCollectionSpec : FunSpec() {
    init {
        test("asBitSequence") {
            checkAll<Int> {
                val value = it.toBigInteger()

                val result = BigIntegerBitCollection(Int.SIZE_BITS).asBitSequence(value)
                val rebuilt = result.foldIndexed(0) { index, acc, bit ->
                    if (bit) {
                        acc or (1 shl index)
                    } else {
                        acc
                    }
                }

                rebuilt shouldBeEqual it
            }
        }

        test("getSetBits") {
            checkAll<Int> {
                val value = it.toBigInteger()

                val result = BigIntegerBitCollection(Int.SIZE_BITS).getSetBits(value)
                val rebuilt = result.fold(0) { acc, pos ->
                    acc or (1 shl pos)
                }

                rebuilt shouldBeEqual it
            }
        }

        test("countLeadingZeroBits") {
            checkAll<Int> {
                val value = it.toBigInteger()

                val result = BigIntegerBitCollection(Int.SIZE_BITS).countLeadingZeroBits(value)

                result shouldBeEqual it.countLeadingZeroBits()
            }
        }

        test("countTrailingZeroBits") {
            checkAll<Int> {
                val value = it.toBigInteger()

                val result = BigIntegerBitCollection(Int.SIZE_BITS).countTrailingZeroBits(value)

                result shouldBeEqual it.countTrailingZeroBits()
            }
        }
    }
}
