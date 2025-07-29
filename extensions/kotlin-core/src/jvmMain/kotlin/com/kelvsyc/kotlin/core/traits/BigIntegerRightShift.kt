package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger

/**
 * Implementation of [RightShift] for a fixed-size bit collection, represented by a [BigInteger].
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerRightShift(private val sized: Sized<BigInteger>) : RightShift<BigInteger> {
    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sized.sizeBits) - BigInteger.ONE
    }

    override fun rightShift(value: BigInteger, bitCount: Int): BigInteger = (value and mask) shr bitCount
}
