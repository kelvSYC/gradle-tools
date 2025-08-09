package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger
import java.util.*

/**
 * Implementation of [ArithmeticRightShift] for a fixed-size bit collection, represented by a [BigInteger].
 *
 * Note that this operation treats its value as being mutable. Use [BitSetArithmeticRightShift] for an operation that
 * treats its value as being immutable.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class MutableBitSetArithmeticRightShift(private val sized: Sized) : ArithmeticRightShift<BitSet> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun arithmeticRightShift(value: BitSet, bitCount: Int): BitSet = value.also {
        val signed = it[sized.sizeBits - 1]
        for (i in 0 ..< sized.sizeBits - bitCount) {
            it[i] = it[i + bitCount]
        }
        it.set(sized.sizeBits - bitCount, sized.sizeBits, signed)
    }
}
