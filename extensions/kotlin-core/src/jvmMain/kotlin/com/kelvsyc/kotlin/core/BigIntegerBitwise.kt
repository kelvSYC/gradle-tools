package com.kelvsyc.kotlin.core

import java.math.BigInteger

/**
 * Implementation of [Bitwise] for types that can be represented as a fixed-size [BigInteger].
 *
 * @param sizeBits The size of the fixed-size [BigInteger].
 */
class BigIntegerBitwise(private val sizeBits: Int) : Bitwise<BigInteger> {
    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sizeBits) - BigInteger.ONE
    }

    override fun and(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) and (rhs and mask)
    override fun or(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) or (rhs and mask)
    override fun xor(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) xor (rhs and mask)
    override fun inv(value: BigInteger): BigInteger = (value and mask).inv() and mask
}
