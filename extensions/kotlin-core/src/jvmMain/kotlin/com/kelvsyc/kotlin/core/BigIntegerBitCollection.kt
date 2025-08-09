package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitCollection
import com.kelvsyc.kotlin.core.traits.Sized
import java.math.BigInteger

/**
 * Implementation of [com.kelvsyc.kotlin.core.traits.BitCollection] on a fixed-size bit collection, backed by a [BigInteger] instance.
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerBitCollection(private val sized: Sized) : BitCollection<BigInteger> {
    override fun fromBits(bits: IntRange): BigInteger {
        require(bits.start >= 0 && bits.endInclusive < sized.sizeBits) { "Bit collection contains values out of range" }

        return bits.fold(BigInteger.ZERO, BigInteger::setBit)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: BigInteger): Sequence<Boolean> = sequence {
        for (i in 0 ..< sized.sizeBits) yield(value.testBit(i))
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asByteArray(value: BigInteger): ByteArray = ByteArray(sized.sizeBits.ceilDiv(Byte.SIZE_BITS)) {
        // Note that value.toByteArray() is not correct, since the endianness is wrong
        // And twos-complement emulation might get in the way
        (0 ..< Byte.SIZE_BITS).fold(0) { acc, i ->
            val pos = it * Byte.SIZE_BITS + i
            if (value.testBit(pos)) (acc or (1 shl i)) else acc
        }.toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: BigInteger): Set<Int> = buildSet {
        for (i in 0 ..< sized.sizeBits) {
            if (value.testBit(i)) add(i)
        }
    }

    override fun countLeadingZeroBits(value: BigInteger): Int {
        // sizeBits - value.bitLength() only works for positive values of BigInteger, due to BigInteger trying to
        // emulate two's complement with a sign-magnitude form internally.
        // Because of complications wrt sizeBits < value.bitLength() and "virtual sign bits", we're just going to do
        // this the clunkier way for now
        return (sized.sizeBits - 1 downTo 0).firstOrNull { value.testBit(it) }?.let {
            sized.sizeBits - 1 - it
        } ?: sized.sizeBits
    }

    override fun countTrailingZeroBits(value: BigInteger): Int = value.lowestSetBit.let {
        if (it == -1) sized.sizeBits else it
    }
}
