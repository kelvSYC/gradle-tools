package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [BitShift] for types that can be represented as a fixed-size bit collection, represented by a
 * [BitSet].
 *
 * Note that all operations return new [BitSet] instances, without modifying the input. Use [MutableBitSetBitShift] for
 * bit shifting operations that mutate its input.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetBitShift(private val sized: Sized) : BitShift<BitSet>,
    LeftShift<BitSet> by BitSetLeftShift(sized),
    RightShift<BitSet> by BitSetRightShift(sized),
    ArithmeticRightShift<BitSet> by BitSetArithmeticRightShift(sized)
