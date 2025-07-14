package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

@OptIn(ExperimentalStdlibApi::class)
class BigIntegerBitRotateSpec : FunSpec() {
    init {
        test("rotateLeft") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val result = BigIntegerBitRotate(Int.SIZE_BITS).rotateLeft(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual value.rotateLeft(bitCount)
            }
        }

        test("rotateRight") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val result = BigIntegerBitRotate(Int.SIZE_BITS).rotateRight(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual value.rotateRight(bitCount)
            }
        }
    }
}
