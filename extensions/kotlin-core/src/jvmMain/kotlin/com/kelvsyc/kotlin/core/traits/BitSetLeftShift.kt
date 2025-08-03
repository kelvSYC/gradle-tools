package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [LeftShift] for a fixed-size bit collection, represented by a [BitSet].
 *
 * Note that this operation returns new [BitSet] instances, without modifying the input. Use [MutableBitSetLeftShift]
 * for an operation that mutates its input.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetLeftShift(private val sized: Sized<BitSet>) : LeftShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun leftShift(value: BitSet, bitCount: Int): BitSet = BitSet(sized.sizeBits).also {
        for (i in bitCount ..< sized.sizeBits) {
            it[i] = value[i - bitCount]
        }
        it.clear(0, bitCount)
    }
}
