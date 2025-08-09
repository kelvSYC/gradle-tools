package com.kelvsyc.kotlin.core.traits

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.math.BigInteger

@OptIn(ExperimentalStdlibApi::class)
class BigIntegerBitRotateSpec : FunSpec() {
    init {
        val sized = object : Sized {
            override val sizeBits: Int = Int.SIZE_BITS
        }
        val traits = BigIntegerBitRotate(sized)

        test("rotateLeft") {
            checkAll(Arb.Companion.int(), Arb.Companion.int(0..<Int.SIZE_BITS)) { value, bitCount ->
                val result = traits.rotateLeft(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual value.rotateLeft(bitCount)
            }
        }

        test("rotateRight") {
            checkAll(Arb.Companion.int(), Arb.Companion.int(0..<Int.SIZE_BITS)) { value, bitCount ->
                val result = traits.rotateRight(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual value.rotateRight(bitCount)
            }
        }
    }
}
