package com.kelvsyc.kotlin.core.traits

import java.util.*

/**
 * Implementation of [BitShift] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all operations treat its value as being mutable. Use [BitSetBitShift] for bit shift operations that treat
 * its value as being immutable.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class MutableBitSetBitShift(private val sized: Sized) : BitShift<BitSet>,
    LeftShift<BitSet> by MutableBitSetLeftShift(sized),
    RightShift<BitSet> by MutableBitSetRightShift(sized),
    ArithmeticRightShift<BitSet> by MutableBitSetArithmeticRightShift(sized)
