package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Bitwise
import java.math.BigInteger

/**
 * Implementation of [Bitwise] for types that can be represented as a fixed-size [BigInteger].
 *
 * @param sized Traits object providing size information on the bit collection.
 */
class BigIntegerBitwise(private val sized: Sized) : Bitwise<BigInteger> {
    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sized.sizeBits) - BigInteger.ONE
    }

    override fun and(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) and (rhs and mask)
    override fun or(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) or (rhs and mask)
    override fun xor(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) xor (rhs and mask)
    override fun inv(value: BigInteger): BigInteger = (value and mask).inv() and mask
}
