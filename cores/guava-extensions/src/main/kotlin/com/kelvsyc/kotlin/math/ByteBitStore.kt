package com.kelvsyc.kotlin.math

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * [BitStore] implementation backed by a [Byte].
 */
@JvmInline
@Suppress("detekt:TooManyFunctions")
value class ByteBitStore(override val bits: Byte) : BitStore<ByteBitStore, Byte> {
    companion object : BitStore.BitStoreConstants<ByteBitStore, Byte> {
        override val sizeBits = Byte.SIZE_BITS

        override val zero = ByteBitStore(0.toByte())
        override val one = ByteBitStore(1.toByte())

        override fun create(bits: Iterable<Int>): ByteBitStore {
            val raw = bits.filter {
                it in 0 ..< sizeBits
            }.fold(0) { acc, bit ->
                acc or (1 shl bit)
            }.toByte()
            return ByteBitStore(raw)
        }

        override fun create(bits: Byte) = ByteBitStore(bits)
    }

    override fun plus(other: ByteBitStore) = ByteBitStore((bits + other.bits).toByte())
    override fun minus(other: ByteBitStore) = ByteBitStore((bits - other.bits).toByte())

    override fun and(other: ByteBitStore) = ByteBitStore(bits and other.bits)
    override fun or(other: ByteBitStore) = ByteBitStore(bits or other.bits)
    override fun xor(other: ByteBitStore) = ByteBitStore(bits xor other.bits)
    override fun inv() = ByteBitStore(bits.inv())

    override fun shl(bitCount: Int) = ByteBitStore((bits.toInt() shl bitCount).toByte())
    override fun shr(bitCount: Int) = ByteBitStore((bits.toInt() shr bitCount).toByte())
    @Suppress("detekt:MagicNumber")
    override fun ushr(bitCount: Int) = ByteBitStore(((bits.toInt() and 0xFF) ushr bitCount).toByte())

    override fun get(position: Int): Boolean {
        check(position in 0..< sizeBits) { "Position must be within range" }
        return bits and (1 shl position).toByte() != 0.toByte()
    }

    override fun asSet(): Set<Int> {
        return (0..sizeBits).filter(this::get).toSet()
    }

    override val trailingZeroes: Int
        get() = bits.countTrailingZeroBits()
}
