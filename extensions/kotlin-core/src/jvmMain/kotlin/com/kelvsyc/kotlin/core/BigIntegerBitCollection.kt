package com.kelvsyc.kotlin.core

import java.math.BigInteger

/**
 * Implementation of [BitCollection] on fixed-size [BigInteger] instances.
 *
 * @param sizeBits The size of the fixed-size [BigInteger].
 */
class BigIntegerBitCollection(override val sizeBits: Int) : BitCollection<BigInteger> {
    override fun fromBits(bits: IntRange): BigInteger {
        require(bits.start >= 0 && bits.endInclusive < sizeBits) { "Bit collection contains values out of range" }

        return bits.fold(BigInteger.ZERO, BigInteger::setBit)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: BigInteger): Sequence<Boolean> = sequence {
        for (i in 0 ..< sizeBits) yield(value.testBit(i))
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asByteArray(value: BigInteger): ByteArray = ByteArray(sizeBits.ceilDiv(Byte.SIZE_BITS)) {
        // Note that value.toByteArray() is not correct, since the endianness is wrong
        // And twos-complement emulation might get in the way
        (0 ..< Byte.SIZE_BITS).fold(0) { acc, i ->
            val pos = it * Byte.SIZE_BITS + i
            if (value.testBit(pos)) (acc or (1 shl i)) else acc
        }.toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: BigInteger): Set<Int> = buildSet {
        for (i in 0 ..< sizeBits) {
            if (value.testBit(i)) add(i)
        }
    }

    override fun isZero(value: BigInteger): Boolean = value == BigInteger.ZERO

    override fun countLeadingZeroBits(value: BigInteger): Int {
        // sizeBits - value.bitLength() only works for positive values of BigInteger, due to BigInteger trying to
        // emulate two's complement with a sign-magnitude form internally.
        // Because of complications wrt sizeBits < value.bitLength() and "virtual sign bits", we're just going to do
        // this the clunkier way for now
        return (sizeBits - 1 downTo 0).firstOrNull { value.testBit(it) }?.let {
            sizeBits - 1 - it
        } ?: sizeBits
    }

    override fun countTrailingZeroBits(value: BigInteger): Int = value.lowestSetBit.let {
        if (it == -1) sizeBits else it
    }
}
