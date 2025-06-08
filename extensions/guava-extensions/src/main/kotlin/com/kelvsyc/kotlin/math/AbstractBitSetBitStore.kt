package com.kelvsyc.kotlin.math

import java.util.*
import kotlin.streams.asSequence

/**
 * Abstract base class for [BitStore] instances backed by a [BitSet].
 *
 * Note that this is not a value class wrapper around a [BitSet], as the size of the bit store needs to be supplied
 * separately.
 */
abstract class AbstractBitSetBitStore<S : AbstractBitSetBitStore<S>> protected constructor(override val bits: BitSet) : BitStore<S, BitSet> {
    abstract class AbstractCompanion<S : AbstractBitSetBitStore<S>> : BitStore.AbstractCompanion<S, BitSet> {
        override fun create(bits: Iterable<Int>): S {
            val raw = BitSet(sizeBits)
            bits.filter { it in 0..< sizeBits }.forEach(raw::set)
            return create(raw)
        }
    }

    protected abstract val traits: AbstractCompanion<S>

    override fun and(other: S): S {
        val raw = BitSet(traits.sizeBits).also {
            it.or(bits)
            it.and(other.bits)
        }
        return traits.create(raw)
    }
    override fun or(other: S): S {
        val raw = BitSet(traits.sizeBits).also {
            it.or(bits)
            it.or(other.bits)
        }
        return traits.create(raw)
    }
    override fun xor(other: S): S {
        val raw = BitSet(traits.sizeBits).also {
            it.or(bits)
            it.xor(other.bits)
        }
        return traits.create(raw)
    }
    override fun inv(): S {
        val raw = BitSet(traits.sizeBits)
        for (i in 0 ..< traits.sizeBits) {
            raw[i] = !bits[i]
        }
        return traits.create(raw)
    }

    override fun shl(bitCount: Int): S {
        val raw = BitSet(traits.sizeBits)
        for (i in bitCount ..< traits.sizeBits) {
            raw[i] = bits[i - bitCount]
        }
        return traits.create(raw)
    }

    override fun shr(bitCount: Int): S {
        val signed = bits[traits.sizeBits - 1]

        val raw = BitSet(traits.sizeBits)
        for (i in 0 ..< traits.sizeBits - bitCount) {
            raw[i] = bits[i + bitCount]
        }
        raw.set(traits.sizeBits - bitCount, traits.sizeBits, signed)
        return traits.create(raw)
    }

    override fun ushr(bitCount: Int): S {
        val raw = BitSet(traits.sizeBits)
        for (i in 0 ..< traits.sizeBits - bitCount) {
            raw[i] = bits[i + bitCount]
        }
        return traits.create(raw)
    }

    override fun get(position: Int): Boolean {
        check(position in 0 ..< traits.sizeBits) { "Bit position $position out of bounds" }
        return bits[position]
    }

    override fun asSet(): Set<Int> {
        return bits.stream().asSequence().toSet()
    }

    override val trailingZeroes by lazy {
        bits.nextSetBit(0)
    }
}
