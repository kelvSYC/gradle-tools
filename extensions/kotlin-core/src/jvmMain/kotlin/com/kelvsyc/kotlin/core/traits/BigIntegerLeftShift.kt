package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger

/**
 * Implementation of [LeftShift] for a fixed-size bit collection, represented by a [BigInteger].
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerLeftShift(private val sized: Sized<BigInteger>) : LeftShift<BigInteger> {
    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sized.sizeBits) - BigInteger.ONE
    }

    override fun leftShift(value: BigInteger, bitCount: Int): BigInteger = (value shl bitCount) and mask
}
