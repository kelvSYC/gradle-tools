package com.kelvsyc.kotlin.core.traits

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.math.BigInteger

@OptIn(ExperimentalStdlibApi::class)
class RightShiftJVMSpec : FunSpec() {
    init {
        context("BigInteger") {
            val sized = object : Sized<BigInteger> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            test("Normal") {
                val traits = BigIntegerRightShift(sized)
                checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                    val result = traits.rightShift(value.toBigInteger(), bitCount)

                    result.toInt() shouldBeEqual (value ushr bitCount)
                }
            }
        }
    }
}
