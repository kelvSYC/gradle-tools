package com.kelvsyc.kotlin.core

import com.google.common.base.Converter
import java.math.BigInteger

/**
 * Implementation of [Bitwise] for types that can be converted into a [BigInteger] that takes up a fixed number of bits.
 */
abstract class AbstractBigIntegerBitwise<T : Any> : AbstractConverterBasedBitwise<T, BigInteger>() {
    /**
     * The number of bits that the converted [BigInteger] takes up.
     */
    abstract val sizeBits: Int

    private val mask by lazy {
        // masking is required in case the converter returns a large negative integer.
        (BigInteger.ONE shl sizeBits) - BigInteger.ONE
    }

    /**
     * [Converter] instance that converts objects of the specified type to the [BigInteger] equivalent.
     *
     * It is not required that the result of the conversion be a non-negative value, as masking operations will be
     * applied where applicable to constrain value to within the size specified by [sizeBits].
     */
    abstract override val converter: Converter<T, BigInteger>

    override fun doAnd(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) and (rhs and mask)
    override fun doOr(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) or (rhs and mask)
    override fun doXor(lhs: BigInteger, rhs: BigInteger): BigInteger = (lhs and mask) xor (rhs and mask)
    override fun doInv(value: BigInteger): BigInteger = (value and mask).inv() and mask

    override fun doLeftShift(value: BigInteger, bitCount: Int): BigInteger = (value shl bitCount) and mask
    override fun doArithmeticRightShift(value: BigInteger, bitCount: Int): BigInteger = (value shr bitCount) and mask
    override fun doLogicalRightShift(value: BigInteger, bitCount: Int): BigInteger = (value and mask) shr bitCount

    override fun doRotateLeft(value: BigInteger, bitCount: Int): BigInteger {
        val n = bitCount % sizeBits
        val left = doLeftShift(value, n)
        val right = doLogicalRightShift(value, sizeBits - n)
        return left or right
    }

    override fun doRotateRight(value: BigInteger, bitCount: Int): BigInteger {
        val n = bitCount % sizeBits
        val left = doLogicalRightShift(value, n)
        val right = doLeftShift(value, sizeBits - n)
        return left or right
    }
}
