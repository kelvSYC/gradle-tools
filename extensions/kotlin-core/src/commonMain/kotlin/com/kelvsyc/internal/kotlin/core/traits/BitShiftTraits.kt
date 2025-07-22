package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.BitShift

object ByteBitShift : BitShift<Byte> {
    override fun leftShift(value: Byte, bitCount: Int): Byte = (value.toInt() shl bitCount).toByte()
    override fun rightShift(value: Byte, bitCount: Int): Byte = (value.toUByte().toInt() ushr bitCount).toByte()
    override fun arithmeticRightShift(value: Byte, bitCount: Int): Byte = (value.toInt() shr bitCount).toByte()
}

object UByteBitShift : BitShift<UByte> {
    override fun leftShift(value: UByte, bitCount: Int): UByte = (value.toInt() shl bitCount).toUByte()
    override fun rightShift(value: UByte, bitCount: Int): UByte = (value.toInt() ushr bitCount).toUByte()
    override fun arithmeticRightShift(value: UByte, bitCount: Int): UByte =
        (value.toByte().toInt() shr bitCount).toUByte()
}

object ShortBitShift : BitShift<Short> {
    override fun leftShift(value: Short, bitCount: Int): Short = (value.toInt() shl bitCount).toShort()
    override fun rightShift(value: Short, bitCount: Int): Short = (value.toUShort().toInt() ushr bitCount).toShort()
    override fun arithmeticRightShift(value: Short, bitCount: Int): Short = (value.toInt() shr bitCount).toShort()
}

object UShortBitShift : BitShift<UShort> {
    override fun leftShift(value: UShort, bitCount: Int): UShort = (value.toInt() shl bitCount).toUShort()
    override fun rightShift(value: UShort, bitCount: Int): UShort = (value.toInt() ushr bitCount).toUShort()
    override fun arithmeticRightShift(value: UShort, bitCount: Int): UShort =
        (value.toShort().toInt() shr bitCount).toUShort()
}

object IntBitShift : BitShift<Int> {
    override fun leftShift(value: Int, bitCount: Int): Int = value shl bitCount
    override fun rightShift(value: Int, bitCount: Int): Int = value ushr bitCount
    override fun arithmeticRightShift(value: Int, bitCount: Int): Int = value shr bitCount
}

object UIntBitShift : BitShift<UInt> {
    override fun leftShift(value: UInt, bitCount: Int): UInt = value shl bitCount
    override fun rightShift(value: UInt, bitCount: Int): UInt = value shr bitCount
    override fun arithmeticRightShift(value: UInt, bitCount: Int): UInt = (value.toInt() shr bitCount).toUInt()
}

object LongBitShift : BitShift<Long> {
    override fun leftShift(value: Long, bitCount: Int): Long = value shl bitCount
    override fun rightShift(value: Long, bitCount: Int): Long = value ushr bitCount
    override fun arithmeticRightShift(value: Long, bitCount: Int): Long = value shr bitCount
}

object ULongBitShift : BitShift<ULong> {
    override fun leftShift(value: ULong, bitCount: Int): ULong = value shl bitCount
    override fun rightShift(value: ULong, bitCount: Int): ULong = value shr bitCount
    override fun arithmeticRightShift(value: ULong, bitCount: Int): ULong = (value.toLong() shr bitCount).toULong()
}
