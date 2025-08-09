package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.IntArrayBitwise
import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

class SyntheticBitRotateSpec : FunSpec() {
    init {
        test("rotateLeft") {
            val sized = object : Sized {
                override val sizeBits: Int
                    get() = Long.SIZE_BITS
            }
            val traits = SyntheticBitRotate(sized, TypeTraits.IntArray, IntArrayBitwise(Long.SIZE_BITS / Int.SIZE_BITS))
            checkAll<Long, Int> { value, bitCount ->
                val bytes = IntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                    (value ushr (it * Int.SIZE_BITS)).toInt()
                }

                val result = traits.rotateLeft(bytes, bitCount)
                val rebuilt = result.foldIndexed(0L) { index, acc, b ->
                    acc or (b.toUInt().toLong() shl (index * Int.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.rotateLeft(bitCount)
            }
        }
        
        test("rotateRight") {
            val sized = object : Sized {
                override val sizeBits: Int
                    get() = Long.SIZE_BITS
            }
            val traits = SyntheticBitRotate(sized, TypeTraits.IntArray, IntArrayBitwise(Long.SIZE_BITS / Int.SIZE_BITS))
            checkAll<Long, Int> { value, bitCount ->
                val bytes = IntArray(Long.SIZE_BITS / Int.SIZE_BITS) {
                    (value ushr (it * Int.SIZE_BITS)).toInt()
                }

                val result = traits.rotateRight(bytes, bitCount)
                val rebuilt = result.foldIndexed(0L) { index, acc, b ->
                    acc or (b.toUInt().toLong() shl (index * Int.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.rotateRight(bitCount)
            }
        }
    }
}
