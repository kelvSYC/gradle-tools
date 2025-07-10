package com.kelvsyc.kotlin.core

import java.math.BigInteger

/**
 * Implementation of [BitShift] for tpes that can be represented as a fixed-size [BigInteger].
 */
abstract class AbstractBigIntegerBitShift : BitShift<BigInteger> {
    /**
     * The size of the fixed-size [BigInteger].
     */
    abstract val sizeBits: Int

    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sizeBits) - BigInteger.ONE
    }

    override fun leftShift(value: BigInteger, bitCount: Int): BigInteger = (value shl bitCount) and mask
    override fun rightShift(value: BigInteger, bitCount: Int): BigInteger = (value and mask) shr bitCount
    override fun arithmeticRightShift(value: BigInteger, bitCount: Int): BigInteger = (value shr bitCount) and mask
}
