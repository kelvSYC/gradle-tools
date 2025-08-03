package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike
import com.kelvsyc.kotlin.core.traits.ArraySized
import kotlin.math.max
import kotlin.math.min

abstract class AbstractArrayBitCollection<A, E>(private val sized: ArraySized<A, E>) : BitCollection<A> {
    abstract val traits: ArrayLike<A, E>
    abstract val base: BitCollection<E>

    override fun fromBits(bits: IntRange): A {
        require(bits.start >= 0 && bits.endInclusive < sized.sizeBits) { "Bit count out of range" }

        return traits.create(sized.arraySize) {
            val startIndex = it * sized.elementSized.sizeBits
            val endIndex = startIndex + sized.elementSized.sizeBits - 1
            val indexRange = startIndex .. endIndex
            val lower = max(bits.start, startIndex)
            val upper = min(bits.endInclusive, endIndex)
            if (indexRange.contains(lower) && indexRange.contains(upper)) {
                val intersection =  lower.rem(sized.elementSized.sizeBits) .. upper.rem(sized.elementSized.sizeBits)
                base.fromBits(intersection)
            } else {
                // The intersection range is definitely empty
                base.fromBits(IntRange.EMPTY) // Simulated zero
            }
        }
    }

    override fun asBitSequence(value: A): Sequence<Boolean> =
        traits.map(value, base::asBitSequence).asSequence().flatten()

    override fun asByteArray(value: A): ByteArray {
        val sizeBytes = sized.elementSized.sizeBytes
        val result = ByteArray(sized.sizeBytes)
        traits.map(value, base::asByteArray).forEachIndexed { index, bytes ->
            // We trust that bytes.size == sizeBytes, but just in case...
            bytes.copyInto(result, index * sizeBytes, 0, sizeBytes)
        }
        return result
    }

    override fun getSetBits(value: A): Set<Int> = buildSet {
        traits.forEachIndexed(value) { index, e ->
            addAll(base.getSetBits(e).map { index * sized.elementSized.sizeBits + it })
        }
    }

    override fun countLeadingZeroBits(value: A): Int {
        val idx = traits.indexOfLast(value) { base.countLeadingZeroBits(it) != sized.elementSized.sizeBits }
        return if (idx == -1) {
            // The array is all zeroes
            sized.sizeBits
        } else {
            (traits.getSize(value) - 1 - idx) * sized.elementSized.sizeBits + base.countLeadingZeroBits(traits.getAt(value, idx))
        }
    }

    override fun countTrailingZeroBits(value: A): Int {
        val idx = traits.indexOfFirst(value) { base.countTrailingZeroBits(it) != sized.elementSized.sizeBits }
        return if (idx == -1) {
            // The array is all zeroes
            sized.sizeBits
        } else {
            idx * sized.elementSized.sizeBits + base.countTrailingZeroBits(traits.getAt(value, idx))
        }
    }
}
