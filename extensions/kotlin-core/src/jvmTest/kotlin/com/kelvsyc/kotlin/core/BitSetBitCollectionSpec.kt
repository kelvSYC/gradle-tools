package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class BitSetBitCollectionSpec : FunSpec() {
    init {
        test("asBitSequence") {
            checkAll<Int> { rawValue ->
                val valueBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rawValue shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val value = BitSet.valueOf(valueBytes)

                val result = BitSetBitCollection(Int.SIZE_BITS).asBitSequence(value)
                val rebuilt = result.foldIndexed(0) { index, acc, bit ->
                    if (bit) {
                        acc or (1 shl index)
                    } else {
                        acc
                    }
                }

                rebuilt shouldBeEqual rawValue
            }
        }

        test("getSetBits") {
            checkAll<Int> { rawValue ->
                val valueBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rawValue shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val value = BitSet.valueOf(valueBytes)

                val result = BitSetBitCollection(Int.SIZE_BITS).getSetBits(value)
                val rebuilt = result.fold(0) { acc, pos ->
                    acc or (1 shl pos)
                }

                rebuilt shouldBeEqual rawValue
            }
        }

        test("countLeadingZeroBits") {
            checkAll<Int> { rawValue ->
                val valueBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rawValue shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val value = BitSet.valueOf(valueBytes)

                val result = BitSetBitCollection(Int.SIZE_BITS).countLeadingZeroBits(value)

                result shouldBeEqual rawValue.countLeadingZeroBits()
            }
        }

        test("countTrailingZeroBits") {
            checkAll<Int> { rawValue ->
                val valueBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rawValue shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val value = BitSet.valueOf(valueBytes)

                val result = BitSetBitCollection(Int.SIZE_BITS).countTrailingZeroBits(value)

                result shouldBeEqual rawValue.countTrailingZeroBits()
            }
        }
    }
}
