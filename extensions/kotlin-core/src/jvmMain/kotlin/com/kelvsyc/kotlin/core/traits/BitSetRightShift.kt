package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [RightShift] for a fixed-size bit collection, represented by a [BitSet].
 *
 * Note that this operation returns new [BitSet] instances, without modifying the input. Use [MutableBitSetRightShift]
 * for an operation that mutates its input.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetRightShift(private val sized: Sized) : RightShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun rightShift(value: BitSet, bitCount: Int): BitSet = BitSet(sized.sizeBits).also {
        for (i in 0 ..< sized.sizeBits - bitCount) {
            it[i] = value[i + bitCount]
        }
        it.clear(sized.sizeBits - bitCount, sized.sizeBits)
    }
}
