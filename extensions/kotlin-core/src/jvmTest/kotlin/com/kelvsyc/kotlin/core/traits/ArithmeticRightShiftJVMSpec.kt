package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.math.BigInteger
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class ArithmeticRightShiftJVMSpec : FunSpec() {
    init {
        context("BigInteger") {
            val sized = object : Sized<BigInteger> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            val traits = BigIntegerArithmeticRightShift(sized)
            test("Normal") {
                checkAll(Arb.int(), Arb.int(0..<Int.SIZE_BITS)) { value, bitCount ->
                    val result = traits.arithmeticRightShift(value.toBigInteger(), bitCount)

                    result.toInt() shouldBeEqual (value shr bitCount)
                }
            }
        }

        context("BitSet") {
            val sized = object : Sized<BitSet> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            val traits = BitSetArithmeticRightShift(sized)
            val mutableTraits = MutableBitSetArithmeticRightShift(sized)
            test("Normal") {
                checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value)
                    val bitset = BitSet.valueOf(bytes)

                    val result = traits.arithmeticRightShift(bitset, bitCount)
                    val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                        acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual (value shr bitCount)
                }
            }
            test("Mutable") {
                checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value)
                    val bitset = BitSet.valueOf(bytes)

                    val result = mutableTraits.arithmeticRightShift(bitset, bitCount)
                    val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                        acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual (value shr bitCount)
                }
            }
        }
    }
}
