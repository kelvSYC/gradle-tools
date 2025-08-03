package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitCollection
import com.kelvsyc.kotlin.core.traits.Sized
import java.util.*
import kotlin.streams.toList

/**
 * Implementation of [com.kelvsyc.kotlin.core.traits.BitCollection] on a fixed-size bit collection, baced by a [BitSet] instance.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BitSetBitCollection(private val sized: Sized<BitSet>) : BitCollection<BitSet> {
    override fun fromBits(bits: IntRange): BitSet {
        require(bits.start >= 0 && bits.endInclusive < sized.sizeBits) { "Bit collection contains values out of range" }

        return BitSet(sized.sizeBits).also {
            bits.forEach(it::set)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: BitSet): Sequence<Boolean> = sequence {
        for (i in 0 ..< value.length()) {
            yield(value.get(i))
        }
    }

    override fun asByteArray(value: BitSet): ByteArray = value.toByteArray().copyOf(sized.sizeBits / Byte.SIZE_BITS)

    override fun getSetBits(value: BitSet): Set<Int> = value.stream().toList().toSet()

    override fun countLeadingZeroBits(value: BitSet): Int = sized.sizeBits - 1 - value.previousSetBit(sized.sizeBits - 1)

    override fun countTrailingZeroBits(value: BitSet): Int = value.nextSetBit(0).let { if (it == -1) sized.sizeBits else it }
}
