package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class BitSetBitwiseSpec : FunSpec() {
    init {
        test("and") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (lhs shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val lhsValue = BitSet.valueOf(lhsBytes)
                val rhsBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rhs shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val rhsValue = BitSet.valueOf(rhsBytes)

                val result = BitSetBitwise(Int.SIZE_BITS).and(lhsValue, rhsValue)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs and rhs)
            }
        }

        test("or") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (lhs shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val lhsValue = BitSet.valueOf(lhsBytes)
                val rhsBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rhs shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val rhsValue = BitSet.valueOf(rhsBytes)

                val result = BitSetBitwise(Int.SIZE_BITS).or(lhsValue, rhsValue)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs or rhs)
            }
        }

        test("xor") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (lhs shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val lhsValue = BitSet.valueOf(lhsBytes)
                val rhsBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rhs shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val rhsValue = BitSet.valueOf(rhsBytes)

                val result = BitSetBitwise(Int.SIZE_BITS).xor(lhsValue, rhsValue)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs xor rhs)
            }
        }

        test("inv") {
            checkAll<Int> { rawValue ->
                val valueBytes = ByteArray(Int.SIZE_BYTES).also {
                    for (i in 0 ..< Int.SIZE_BYTES) {
                        it[i] = (rawValue shr (i * Byte.SIZE_BITS)).toByte()
                    }
                }
                val value = BitSet.valueOf(valueBytes)

                val result = BitSetBitwise(Int.SIZE_BITS).inv(value)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual rawValue.inv()
            }
        }
    }
}
