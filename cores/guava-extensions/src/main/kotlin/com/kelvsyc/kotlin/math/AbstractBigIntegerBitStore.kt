package com.kelvsyc.kotlin.math

import java.math.BigInteger

/**
 * Abstract base class for [BitStore] instances backed by a [BigInteger].
 *
 * Note that this is not a value class wrapper around a [BigInteger], as the size of the bit store needs to be supplied
 * separately.
 */
abstract class AbstractBigIntegerBitStore<S : AbstractBigIntegerBitStore<S>>(override val bits: BigInteger) : BitStore<S, BigInteger> {
    abstract class BitStoreConstants<S : AbstractBigIntegerBitStore<S>> : BitStore.BitStoreConstants<S, BigInteger> {
        override val zero by lazy { create(BigInteger.ZERO) }
        override val one by lazy { create(BigInteger.ONE) }

        override fun create(bits: Iterable<Int>): S {
            val raw = bits.fold(BigInteger.ZERO, BigInteger::setBit)
            return create(raw)
        }
    }

    protected abstract val traits: BitStoreConstants<S>

    override fun plus(other: S): S = traits.create(bits + other.bits)
    override fun minus(other: S): S = traits.create(bits - other.bits)

    override fun and(other: S): S = traits.create(bits and other.bits)
    override fun or(other: S): S = traits.create(bits or other.bits)
    override fun xor(other: S): S = traits.create(bits xor other.bits)
    override fun inv(): S = traits.create(bits.inv())

    override fun shl(bitCount: Int): S = traits.create(bits shl bitCount)
    override fun shr(bitCount: Int): S = traits.create(bits shr bitCount)
    override fun ushr(bitCount: Int): S {
        val signedShift = bits shr bitCount
        // BigInteger only offers signed bit shifts, so we simulate the fill
        val raw = ((traits.sizeBits - bitCount)..traits.sizeBits).fold(signedShift, BigInteger::clearBit)
        return traits.create(raw)
    }

    override fun get(position: Int) = bits.testBit(position)
}
