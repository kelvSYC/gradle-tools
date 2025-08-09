package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [RightShift] for a fixed-size bit collection, represented by a [BitSet].
 *
 * Note that this operation treats its value as being mutable. Use [BitSetRightShift] for an operation that treats its
 * value as being immutable.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class MutableBitSetRightShift(private val sized: Sized) : RightShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun rightShift(value: BitSet, bitCount: Int): BitSet = value.also {
        for (i in 0 ..< sized.sizeBits - bitCount) {
            it[i] = it[i + bitCount]
        }
        it.clear(sized.sizeBits - bitCount, sized.sizeBits)
    }
}
