package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class MutableBitSetBitShiftSpec : FunSpec() {
    init {
        test("leftShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = ByteArray(Int.SIZE_BYTES)
                for (i in 0 ..< Int.SIZE_BYTES) {
                    bytes[i] = (value shr (i * Byte.SIZE_BITS)).toByte()
                }
                val bitset = BitSet.valueOf(bytes)

                val result = MutableBitSetBitShift(Int.SIZE_BITS).leftShift(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (value shl bitCount)
            }
        }

        test("rightShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = ByteArray(Int.SIZE_BYTES)
                for (i in 0 ..< Int.SIZE_BYTES) {
                    bytes[i] = (value shr (i * Byte.SIZE_BITS)).toByte()
                }
                val bitset = BitSet.valueOf(bytes)

                val result = MutableBitSetBitShift(Int.SIZE_BITS).rightShift(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (value ushr bitCount)
            }
        }

        test("arithmeticRightShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = ByteArray(Int.SIZE_BYTES)
                for (i in 0 ..< Int.SIZE_BYTES) {
                    bytes[i] = (value shr (i * Byte.SIZE_BITS)).toByte()
                }
                val bitset = BitSet.valueOf(bytes)

                val result = MutableBitSetBitShift(Int.SIZE_BITS).arithmeticRightShift(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (value shr bitCount)
            }
        }
    }
}
