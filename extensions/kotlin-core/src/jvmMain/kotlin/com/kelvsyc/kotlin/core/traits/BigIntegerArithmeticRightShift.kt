package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger

/**
 * Implementation of [ArithmeticRightShift] for a fixed-size bit collection, represented by a [BigInteger].
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerArithmeticRightShift(private val sized: Sized) : ArithmeticRightShift<BigInteger> {
    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sized.sizeBits) - BigInteger.ONE
    }

    override fun arithmeticRightShift(value: BigInteger, bitCount: Int): BigInteger = (value shr bitCount) and mask
}
