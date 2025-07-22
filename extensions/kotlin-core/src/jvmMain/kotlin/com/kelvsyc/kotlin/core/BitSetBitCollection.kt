package com.kelvsyc.kotlin.core

import java.util.*
import kotlin.streams.toList

/**
 * Implementation of [BitCollection] on fixed-size [BitSet] instances.
 *
 * @param sizeBits The size of the fixed-size [BitSet].
 */
class BitSetBitCollection(private val sizeBits: Int) : BitCollection<BitSet> {
    override fun fromBits(bits: IntRange): BitSet {
        require(bits.start >= 0 && bits.endInclusive < sizeBits) { "Bit collection contains values out of range" }

        return BitSet(sizeBits).also {
            bits.forEach(it::set)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: BitSet): Sequence<Boolean> = sequence {
        for (i in 0 ..< value.length()) {
            yield(value.get(i))
        }
    }

    override fun asByteArray(value: BitSet): ByteArray = value.toByteArray().copyOf(sizeBits)

    override fun getSetBits(value: BitSet): Set<Int> = value.stream().toList().toSet()

    override fun isZero(value: BitSet): Boolean = value.isEmpty

    override fun countLeadingZeroBits(value: BitSet): Int = sizeBits - 1 - value.previousSetBit(sizeBits - 1)

    override fun countTrailingZeroBits(value: BitSet): Int = value.nextSetBit(0).let { if (it == -1) sizeBits else it }
}
