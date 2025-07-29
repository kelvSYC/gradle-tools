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
class LeftShiftJVMSpec : FunSpec() {
    init {
        context("BigInteger") {
            val sized = object : Sized<BigInteger> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            val traits = BigIntegerBitShift(sized)
            test("Normal") {
                checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                    val result = traits.leftShift(value.toBigInteger(), bitCount)

                    result.toInt() shouldBeEqual (value shl bitCount)
                }
            }
        }

        context("BitSet") {
            val sized = object : Sized<BitSet> {
                override val sizeBits: Int = Int.SIZE_BITS
            }
            val traits = BitSetBitShift(sized)
            val mutableTraits = MutableBitSetBitShift(sized)
            test("Normal") {
                checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value)
                    val bitset = BitSet.valueOf(bytes)

                    val result = traits.leftShift(bitset, bitCount)
                    val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                        acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual (value shl bitCount)
                }
            }
            test("Mutable") {
                checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                    val bytes = TypeTraits.Int.asByteArray(value)
                    val bitset = BitSet.valueOf(bytes)

                    val result = mutableTraits.leftShift(bitset, bitCount)
                    val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                        acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                    }

                    rebuilt shouldBeEqual (value shl bitCount)
                }
            }
        }
    }
}
