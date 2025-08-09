package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import java.util.*

class MutableBitSetBitwiseSpec : FunSpec() {
    init {
        val sized = object : Sized {
            override val sizeBits: Int = Int.SIZE_BITS
        }
        val traits = BitSetBitwise(sized)

        test("and") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val lhsValue = BitSet.valueOf(lhsBytes)
                val rhsBytes = TypeTraits.Int.asByteArray(rhs)
                val rhsValue = BitSet.valueOf(rhsBytes)

                val result = traits.and(lhsValue, rhsValue)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs and rhs)
            }
        }

        test("or") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val lhsValue = BitSet.valueOf(lhsBytes)
                val rhsBytes = TypeTraits.Int.asByteArray(rhs)
                val rhsValue = BitSet.valueOf(rhsBytes)

                val result = traits.or(lhsValue, rhsValue)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs or rhs)
            }
        }

        test("xor") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val lhsValue = BitSet.valueOf(lhsBytes)
                val rhsBytes = TypeTraits.Int.asByteArray(rhs)
                val rhsValue = BitSet.valueOf(rhsBytes)

                val result = traits.xor(lhsValue, rhsValue)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs xor rhs)
            }
        }

        test("inv") {
            checkAll<Int> { rawValue ->
                val valueBytes = TypeTraits.Int.asByteArray(rawValue)
                val value = BitSet.valueOf(valueBytes)

                val result = traits.inv(value)
                val rebuilt = result.toByteArray().foldIndexed(0) { index, acc, b ->
                    acc or (b.toUByte().toInt() shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual rawValue.inv()
            }
        }
    }
}
