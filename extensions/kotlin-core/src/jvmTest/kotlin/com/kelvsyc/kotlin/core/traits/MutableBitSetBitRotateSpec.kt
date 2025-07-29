package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
class MutableBitSetBitRotateSpec : FunSpec() {
    init {
        val sized = object : Sized<BitSet> {
            override val sizeBits: Int = Int.SIZE_BITS
        }
        val traits = MutableBitSetBitRotate(sized)

        test("rotateLeft") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = TypeTraits.Int.asByteArray(value)
                val bitset = BitSet.valueOf(bytes)

                val result = traits.rotateLeft(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.rotateLeft(bitCount)
            }
        }

        test("rotateRight") {
            checkAll(Arb.int(), Arb.int(0 ..< Int.SIZE_BITS)) { value, bitCount ->
                val bytes = TypeTraits.Int.asByteArray(value)
                val bitset = BitSet.valueOf(bytes)

                val result =traits.rotateRight(bitset, bitCount)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.rotateRight(bitCount)
            }
        }
    }
}
