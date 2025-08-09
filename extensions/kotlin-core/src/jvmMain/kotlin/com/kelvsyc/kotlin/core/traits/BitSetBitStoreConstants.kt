package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [BitStoreConstants] for a fixed-size bit collection, backed by a [BitSet].
 */
class BitSetBitStoreConstants(private val sized: Sized) : BitStoreConstants<BitSet> {
    override val allClear: BitSet = BitSet(sized.sizeBits)
    override val allSet: BitSet by lazy {
        BitSet(sized.sizeBits).also {
            it.set(0, sized.sizeBits - 1)
        }
    }
    override fun hasSetBits(value: BitSet): Boolean = !value.isEmpty
    override fun isAllClear(value: BitSet): Boolean = value.isEmpty
}
