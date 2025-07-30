package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Bitwise
import java.util.*

/**
 * Implementation of [Bitwise] for types that can be represented as a fixed-size bit collection, represented by a
 * [BitSet].
 *
 * Note that all operations return new [BitSet] instances, without modifying the input. Use [MutableBitSetBitwise] for
 * bitwise operations that mutate its input.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetBitwise(private val sized: Sized<BitSet>) : Bitwise<BitSet> {
    override fun and(lhs: BitSet, rhs: BitSet): BitSet = BitSet(sized.sizeBits).also {
        it.or(lhs)
        it.and(rhs)
    }
    override fun or(lhs: BitSet, rhs: BitSet): BitSet = BitSet(sized.sizeBits).also {
        it.or(lhs)
        it.or(rhs)
    }
    override fun xor(lhs: BitSet, rhs: BitSet): BitSet = BitSet(sized.sizeBits).also {
        it.or(lhs)
        it.xor(rhs)
    }
    override fun inv(value: BitSet): BitSet = BitSet(sized.sizeBits).also {
        it.or(value)
        it.flip(0, sized.sizeBits)
    }
}
