package com.kelvsyc.kotlin.core

import java.util.*

/**
 * Implementation of [Bitwise] for types that can be represented as a fixed-size [BitSet].
 *
 * Note that all unary operations treat its operand as being mutable, and all binary operations treat its left operand
 * as being mutable.
 *
 * @param sizeBits The size of the fixed-size [BitSet].
 */
class BitSetBitwise(private val sizeBits: Int) : Bitwise<BitSet> {
    override fun and(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.and(rhs) }
    override fun or(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.or(rhs) }
    override fun xor(lhs: BitSet, rhs: BitSet): BitSet = lhs.also { it.xor(rhs) }
    override fun inv(value: BitSet): BitSet = value.also { it.flip(0, sizeBits) }
}
