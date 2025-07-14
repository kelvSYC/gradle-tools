package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

@OptIn(ExperimentalStdlibApi::class)
class BigIntegerBitShiftSpec : FunSpec() {
    init {
        test("leftShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val result = BigIntegerBitShift(Int.SIZE_BITS).leftShift(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual (value shl bitCount)
            }
        }

        test("rightShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val result = BigIntegerBitShift(Int.SIZE_BITS).rightShift(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual (value ushr bitCount)
            }
        }

        test("arithmeticRightShift") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val result = BigIntegerBitShift(Int.SIZE_BITS).arithmeticRightShift(value.toBigInteger(), bitCount)

                result.toInt() shouldBeEqual (value shr bitCount)
            }
        }
    }
}
