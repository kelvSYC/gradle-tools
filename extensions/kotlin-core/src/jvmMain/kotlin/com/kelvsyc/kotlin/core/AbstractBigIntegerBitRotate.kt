package com.kelvsyc.kotlin.core

import java.math.BigInteger

/**
 * Implementation of [BitRotate] for tpes that can be represented as a fixed-size [BigInteger].
 *
 * The implementation relies on a suitable [BitShift] to supply the underlying bit shifting operations.
 *
 * @param bitShift Implementation of [BitShift] providing the bit shifting operations.
 */
abstract class AbstractBigIntegerBitRotate(private val bitShift: BitShift<BigInteger>) : BitRotate<BigInteger> {
    /**
     * The size of the fixed-size [BigInteger].
     */
    abstract val sizeBits: Int

    override fun rotateLeft(value: BigInteger, bitCount: Int): BigInteger {
        val n = bitCount % sizeBits
        val left = bitShift.leftShift(value, n)
        val right = bitShift.rightShift(value, sizeBits - n)
        return left or right
    }

    override fun rotateRight(value: BigInteger, bitCount: Int): BigInteger {
        val n = bitCount % sizeBits
        val left = bitShift.rightShift(value, n)
        val right = bitShift.leftShift(value, sizeBits - n)
        return left or right
    }
}
