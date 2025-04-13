package com.kelvsyc.kotlin.math

/**
 * [BitStore] implementation backed by a [Long]
 */
@JvmInline
@Suppress("detekt:TooManyFunctions")
value class LongBitStore(override val bits: Long) : BitStore<LongBitStore, Long> {
    companion object : BitStore.BitStoreConstants<LongBitStore, Long> {
        override val sizeBits = Long.SIZE_BITS

        override val zero = LongBitStore(0)
        override val one = LongBitStore(1)

        override fun create(bits: Iterable<Int>): LongBitStore {
            val raw = bits.filter {
                it in 0 ..< sizeBits
            }.fold(0L) { acc, bit ->
                acc or (1L shl bit)
            }
            return LongBitStore(raw)
        }

        override fun create(bits: Long) = LongBitStore(bits)
    }

    override fun plus(other: LongBitStore) = LongBitStore(bits + other.bits)
    override fun minus(other: LongBitStore) = LongBitStore(bits - other.bits)

    override fun and(other: LongBitStore) = LongBitStore(bits and other.bits)
    override fun or(other: LongBitStore) = LongBitStore(bits or other.bits)
    override fun xor(other: LongBitStore) = LongBitStore(bits xor other.bits)
    override fun inv() = LongBitStore(bits.inv())

    override fun shl(bitCount: Int) = LongBitStore(bits shl bitCount)
    override fun shr(bitCount: Int) = LongBitStore(bits shr bitCount)
    override fun ushr(bitCount: Int) = LongBitStore(bits ushr bitCount)

    override fun get(position: Int): Boolean {
        check(position in 0..< sizeBits) { "Position must be within range" }
        return bits and (1L shl position) != 0L
    }

    override fun asSet(): Set<Int> {
        return (0..sizeBits).filter(this::get).toSet()
    }

    override val trailingZeroes: Int
        get() = bits.countTrailingZeroBits()
}
