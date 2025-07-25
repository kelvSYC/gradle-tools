package com.kelvsyc.kotlin.core

import java.util.*

/**
 * Implementation of [BitShift] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all operations treat its value as being mutable. Use [BitSetBitShift] for bitwise operations that treat its
 * value as being immutable.
 *
 * @param sizeBits The size of the fixed-size [BitSet].
 */
class MutableBitSetBitShift(private val sizeBits: Int) : BitShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun leftShift(value: BitSet, bitCount: Int): BitSet = value.also {
        for (i in sizeBits - 1 downTo bitCount) {
            it[i] = it[i - bitCount]
        }
        it.clear(0, bitCount)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun rightShift(value: BitSet, bitCount: Int): BitSet = value.also {
        for (i in 0 ..< sizeBits - bitCount) {
            it[i] = it[i + bitCount]
        }
        it.clear(sizeBits - bitCount, sizeBits)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun arithmeticRightShift(value: BitSet, bitCount: Int): BitSet = value.also {
        val signed = it[sizeBits - 1]
        for (i in 0 ..< sizeBits - bitCount) {
            it[i] = it[i + bitCount]
        }
        it.set(sizeBits - bitCount, sizeBits, signed)
    }
}
