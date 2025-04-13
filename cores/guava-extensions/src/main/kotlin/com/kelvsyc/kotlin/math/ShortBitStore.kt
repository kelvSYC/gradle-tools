package com.kelvsyc.kotlin.math

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * [BitStore] implementation backed by a [Short].
 */
@JvmInline
value class ShortBitStore(override val bits: Short) : BitStore<ShortBitStore, Short> {
    companion object : BitStore.BitStoreConstants<ShortBitStore, Short> {
        override val sizeBits = Short.SIZE_BITS

        override val zero = ShortBitStore(0.toShort())
        override val one = ShortBitStore(1.toShort())

        override fun create(bits: Iterable<Int>): ShortBitStore {
            val raw = bits.filter {
                it in 0 ..< sizeBits
            }.fold(0) { acc, bit ->
                acc or (1 shl bit)
            }.toShort()
            return ShortBitStore(raw)
        }

        override fun create(bits: Short) = ShortBitStore(bits)
    }

    override fun plus(other: ShortBitStore) = ShortBitStore((bits + other.bits).toShort())
    override fun minus(other: ShortBitStore) = ShortBitStore((bits - other.bits).toShort())

    override fun and(other: ShortBitStore) = ShortBitStore(bits and other.bits)
    override fun or(other: ShortBitStore) = ShortBitStore(bits or other.bits)
    override fun xor(other: ShortBitStore) = ShortBitStore(bits xor other.bits)
    override fun inv() = ShortBitStore(bits.inv())

    override fun shl(bitCount: Int) = ShortBitStore((bits.toInt() shl bitCount).toShort())
    override fun shr(bitCount: Int) = ShortBitStore((bits.toInt() shr bitCount).toShort())
    override fun ushr(bitCount: Int) = ShortBitStore(((bits.toInt() and 0xFFFF) ushr bitCount).toShort())

    override fun get(position: Int): Boolean {
        check(position in 0..< sizeBits) { "Position must be within range" }
        return bits and (1 shl position).toShort() != 0.toShort()
    }

    override fun asSet(): Set<Int> {
        return (0..sizeBits).filter(this::get).toSet()
    }

    override val trailingZeroes: Int
        get() = bits.countTrailingZeroBits()
}
