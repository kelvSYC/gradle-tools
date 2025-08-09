package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.Sized
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.intRange
import io.kotest.property.checkAll
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class BitSetBitCollectionSpec : FunSpec() {
    init {
        val sized = object : Sized {
            override val sizeBits: Int = Int.SIZE_BITS
        }
        val traits = BitSetBitCollection(sized)

        test("fromBits") {
            val arb = Arb.intRange(0 ..< Int.SIZE_BITS)
            checkAll(arb) {
                val result = traits.fromBits(it)
                it.forEach {
                    result.get(it).shouldBeTrue()
                }
            }
        }

        test("asBitSequence") {
            checkAll<Int> {
                val valueBytes = TypeTraits.Int.asByteArray(it)
                val value = BitSet.valueOf(valueBytes)

                val result = traits.asBitSequence(value)
                val rebuilt = result.foldIndexed(0) { index, acc, bit ->
                    if (bit) {
                        acc or (1 shl index)
                    } else {
                        acc
                    }
                }

                rebuilt shouldBeEqual it
            }
        }

        test("asByteArray") {
            checkAll<Int> {
                val valueBytes = TypeTraits.Int.asByteArray(it)
                val value = BitSet.valueOf(valueBytes)

                val result = traits.asByteArray(value)
                val expected = TypeTraits.Int.asByteArray(it)

                result.toList() shouldBeEqual expected.toList()
            }
        }

        test("getSetBits") {
            checkAll<Int> {
                val valueBytes = TypeTraits.Int.asByteArray(it)
                val value = BitSet.valueOf(valueBytes)

                val result = traits.getSetBits(value)
                val rebuilt = result.fold(0) { acc, pos ->
                    acc or (1 shl pos)
                }

                rebuilt shouldBeEqual it
            }
        }
        test("countLeadingZeroBits") {
            checkAll<Int> {
                val valueBytes = TypeTraits.Int.asByteArray(it)
                val value = BitSet.valueOf(valueBytes)

                val result = traits.countLeadingZeroBits(value)

                result shouldBeEqual it.countLeadingZeroBits()
            }
        }

        test("countTrailingZeroBits") {
            checkAll<Int> {
                val valueBytes = TypeTraits.Int.asByteArray(it)
                val value = BitSet.valueOf(valueBytes)

                val result = traits.countTrailingZeroBits(value)

                result shouldBeEqual it.countTrailingZeroBits()
            }
        }
    }
}
