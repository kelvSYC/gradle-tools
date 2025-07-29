package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [BitRotate] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all operations return new [BitSet] instances, without modifying the input. Use [MutableBitSetBitRotate] for
 * bit rotation operations that mutate its input.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetBitRotate(private val sized: Sized<BitSet>) : BitRotate<BitSet> {
    override fun rotateLeft(value: BitSet, bitCount: Int): BitSet {
        val setBits = generateSequence(seedFunction = {
            value.nextSetBit(0).takeIf { it != -1 }
        }) {
            it.takeIf { it != Int.MAX_VALUE }?.let {
                value.nextSetBit(it + 1).takeIf { it != -1 }
            }
        }

        val result = BitSet(sized.sizeBits).also {
            setBits.map { (it + bitCount).mod(sized.sizeBits) }.forEach(it::set)
        }
        return result
    }

    override fun rotateRight(value: BitSet, bitCount: Int): BitSet {
        // In-place rotation algorithm can accommodate negative rotations, so we do a negative left rotation.
        return rotateLeft(value, -bitCount)
    }
}
