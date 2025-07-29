package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.Bitwise
import java.util.*

/**
 * Implementation of [Bitwise] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all unary operations treat its operand as being mutable, and all binary operations treat its left operand
 * as being mutable. Use [BitSetBitwise] for bitwise operations that treat its value as being immutable.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class MutableBitSetBitwise(private val sized: Sized<BitSet>) : Bitwise<BitSet> {
    override fun and(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.and(rhs) }
    override fun or(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.or(rhs) }
    override fun xor(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.xor(rhs) }
    override fun inv(value: BitSet): BitSet = value.also { it.flip(0, sized.sizeBits) }
}
