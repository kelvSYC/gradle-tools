package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

@OptIn(ExperimentalStdlibApi::class)
class ByteArrayBitShiftSpec : FunSpec() {
    init {
        test("leftShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = ByteArray(Int.SIZE_BYTES)
                for (i in 0 ..< Int.SIZE_BYTES) {
                    bytes[i] = (value shr (i * Byte.SIZE_BITS)).toByte()
                }

                val result = ByteArrayBitShift.leftShift(bytes, bitCount)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
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

                val result = ByteArrayBitShift.rightShift(bytes, bitCount)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
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

                val result = ByteArrayBitShift.arithmeticRightShift(bytes, bitCount)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (value shr bitCount)
            }
        }
    }
}
