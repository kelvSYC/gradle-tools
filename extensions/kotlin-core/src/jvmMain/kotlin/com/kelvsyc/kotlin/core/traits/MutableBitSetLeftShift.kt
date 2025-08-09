package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [LeftShift] for a fixed-size bit collection, represented by a [BitSet].
 *
 * Note that this operation treats its value as being mutable. Use [BitSetLeftShift] for an operation that treats its
 * value as being immutable.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class MutableBitSetLeftShift(private val sized: Sized) : LeftShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun leftShift(value: BitSet, bitCount: Int): BitSet = value.also {
        for (i in sized.sizeBits - 1 downTo bitCount) {
            it[i] = it[i - bitCount]
        }
        it.clear(0, bitCount)
    }
}
