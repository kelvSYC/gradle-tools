package com.kelvsyc.kotlin.core.traits

import java.math.BigInteger

/**
 * Implementation of [BitStoreConstants] for a fixed-size bit collection, backed by a [BigInteger].
 */
class BigIntegerBitStoreConstants(private val sized: Sized) : BitStoreConstants<BigInteger> {
    override val allClear: BigInteger = BigInteger.ZERO
    override val allSet: BigInteger by lazy {
        (BigInteger.ONE shl sized.sizeBits) - BigInteger.ONE
    }
    override fun isAllClear(value: BigInteger): Boolean = value == allClear
    override fun hasSetBits(value: BigInteger): Boolean = value != allClear
}
