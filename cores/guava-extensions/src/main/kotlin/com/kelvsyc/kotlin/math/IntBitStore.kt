package com.kelvsyc.kotlin.math

/**
 * [BitStore] implementation backed by an [Int]
 */
@JvmInline
value class IntBitStore(override val bits: Int) : BitStore<IntBitStore, Int> {
    companion object : BitStore.BitStoreConstants<IntBitStore, Int> {
        override val sizeBits = Int.SIZE_BITS

        override val zero = IntBitStore(0)
        override val one = IntBitStore(1)

        override fun create(bits: Iterable<Int>): IntBitStore {
            val raw = bits.filter {
                it in 0 ..< sizeBits
            }.fold(0) { acc, bit ->
                acc or (1 shl bit)
            }
            return IntBitStore(raw)
        }

        override fun create(bits: Int) = IntBitStore(bits)
    }

    override fun plus(other: IntBitStore) = IntBitStore(bits + other.bits)
    override fun minus(other: IntBitStore) = IntBitStore(bits - other.bits)

    override fun and(other: IntBitStore) = IntBitStore(bits and other.bits)
    override fun or(other: IntBitStore) = IntBitStore(bits or other.bits)
    override fun xor(other: IntBitStore) = IntBitStore(bits xor other.bits)
    override fun inv() = IntBitStore(bits.inv())

    override fun shl(bitCount: Int) = IntBitStore(bits shl bitCount)
    override fun shr(bitCount: Int) = IntBitStore(bits shr bitCount)
    override fun ushr(bitCount: Int) = IntBitStore(bits ushr bitCount)

    override fun get(position: Int): Boolean {
        check(position in 0..< sizeBits) { "Position must be within range" }
        return bits and (1 shl position) != 0
    }
}
