package com.kelvsyc.kotlin.core

import java.math.BigInteger

/**
 * Implementation of [BitCollection] on fixed-size [BigInteger] instances.
 *
 * @param sizeBits The size of the fixed-size [BigInteger].
 */
class BigIntegerBitCollection(private val sizeBits: Int) : BitCollection<BigInteger> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: BigInteger): Sequence<Boolean> = sequence {
        for (i in 0 ..< sizeBits) yield(value.testBit(i))
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: BigInteger): Set<Int> = buildSet {
        for (i in 0 ..< sizeBits) {
            if (value.testBit(i)) add(i)
        }
    }

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
