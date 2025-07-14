package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class MutableBitSetBitRotateSpec : FunSpec() {
    init {
        test("rotateLeft") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = ByteArray(Int.SIZE_BYTES)
                for (i in 0 ..< Int.SIZE_BYTES) {
                    bytes[i] = (value shr (i * Byte.SIZE_BITS)).toByte()
                }
                val bitset = BitSet.valueOf(bytes)

                val result = MutableBitSetBitRotate(Int.SIZE_BITS).rotateLeft(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.rotateLeft(bitCount)
            }
        }

        test("rotateRight") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = ByteArray(Int.SIZE_BYTES)
                for (i in 0 ..< Int.SIZE_BYTES) {
                    bytes[i] = (value shr (i * Byte.SIZE_BITS)).toByte()
                }
                val bitset = BitSet.valueOf(bytes)

                val result = MutableBitSetBitRotate(Int.SIZE_BITS).rotateRight(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.rotateRight(bitCount)
            }
        }
    }
}
