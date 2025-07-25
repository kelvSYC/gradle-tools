package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike
import kotlin.math.max
import kotlin.math.min

abstract class AbstractArrayBitCollection<A, E>(private val size: Int) : BitCollection<A> {
    abstract val traits: ArrayLike<A, E>
    abstract val base: BitCollection<E>

    override val sizeBits: Int by lazy { size * base.sizeBits }

    override fun fromBits(bits: IntRange): A {
        require(bits.start >= 0 && bits.endInclusive < sizeBits) { "Bit count out of range" }

        return traits.create(size) {
            val startIndex = it * base.sizeBits
            val endIndex = startIndex + base.sizeBits - 1
            val indexRange = startIndex .. endIndex
            val lower = max(bits.start, startIndex)
            val upper = min(bits.endInclusive, endIndex)
            if (indexRange.contains(lower) && indexRange.contains(upper)) {
                val intersection =  lower.rem(base.sizeBits) .. upper.rem(base.sizeBits)
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
        val sizeBytes = base.sizeBits / Byte.SIZE_BITS
        val result = ByteArray(size * (base.sizeBits / Byte.SIZE_BITS))
        traits.map(value, base::asByteArray).forEachIndexed { index, bytes ->
            // We trust that bytes.size == sizeBytes, but just in case...
            bytes.copyInto(result, index * sizeBytes, 0, sizeBytes)
        }
        return result
    }

    override fun getSetBits(value: A): Set<Int> = buildSet {
        traits.forEachIndexed(value) { index, e ->
            addAll(base.getSetBits(e).map { index * base.sizeBits + it })
        }
    }

    override fun isZero(value: A): Boolean = traits.all(value, base::isZero)

    override fun countLeadingZeroBits(value: A): Int {
        val idx = traits.indexOfLast(value) { base.countLeadingZeroBits(it) != base.sizeBits }
        return if (idx == -1) {
            // The array is all zeroes
            traits.getSize(value) * base.sizeBits
        } else {
            (traits.getSize(value) - 1 - idx) * base.sizeBits + base.countLeadingZeroBits(traits.getAt(value, idx))
        }
    }

    override fun countTrailingZeroBits(value: A): Int {
        val idx = traits.indexOfFirst(value) { base.countTrailingZeroBits(it) != base.sizeBits }
        return if (idx == -1) {
            // The array is all zeroes
            traits.getSize(value) * base.sizeBits
        } else {
            idx * base.sizeBits + base.countTrailingZeroBits(traits.getAt(value, idx))
        }
    }
}
