package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger
import java.util.*

/**
 * Implementation of [ArithmeticRightShift] for a fixed-size bit collection, represented by a [BigInteger].
 *
 * Note that this operation returns new [BitSet] instances, without modifying the input. Use
 * [MutableBitSetArithmeticRightShift] for an operation that mutates its input.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetArithmeticRightShift(private val sized: Sized<BitSet>) : ArithmeticRightShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun arithmeticRightShift(value: BitSet, bitCount: Int): BitSet = BitSet(sized.sizeBits).also {
        val signed = value[sized.sizeBits - 1]
        for (i in 0 ..< sized.sizeBits - bitCount) {
            it[i] = value[i + bitCount]
        }
        it.set(sized.sizeBits - bitCount, sized.sizeBits, signed)
    }
}
