package com.kelvsyc.kotlin.core

import java.util.*

/**
 * Implementation of [BitShift] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all operations return new [BitSet] instances, without modifying the input. Use [MutableBitSetBitShift] for
 * bit shifting operations that mutate its input.
 *
 * @param sizeBits The size of the fixed-size [BitSet].
 */
class BitSetBitShift(private val sizeBits: Int) : BitShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun leftShift(value: BitSet, bitCount: Int): BitSet = BitSet(sizeBits).also {
        for (i in bitCount ..< sizeBits) {
            it[i] = value[i - bitCount]
        }
        it.clear(0, bitCount)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun rightShift(value: BitSet, bitCount: Int): BitSet = BitSet(sizeBits).also {
        for (i in 0 ..< sizeBits - bitCount) {
            it[i] = value[i + bitCount]
        }
        it.clear(sizeBits - bitCount, sizeBits)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun arithmeticRightShift(value: BitSet, bitCount: Int): BitSet = BitSet(sizeBits).also {
        val signed = value[sizeBits - 1]
        for (i in 0 ..< sizeBits - bitCount) {
            it[i] = value[i + bitCount]
        }
        it.set(sizeBits - bitCount, sizeBits, signed)
    }
}
