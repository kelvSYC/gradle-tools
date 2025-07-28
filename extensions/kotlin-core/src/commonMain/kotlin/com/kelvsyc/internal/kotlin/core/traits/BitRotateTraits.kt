package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.BitRotate

interface ByteBitRotate : BitRotate<Byte> {
    override fun rotateLeft(value: Byte, bitCount: Int): Byte = value.rotateLeft(bitCount)
    override fun rotateRight(value: Byte, bitCount: Int): Byte = value.rotateRight(bitCount)
}

interface UByteBitRotate : BitRotate<UByte> {
    override fun rotateLeft(value: UByte, bitCount: Int): UByte = value.rotateLeft(bitCount)
    override fun rotateRight(value: UByte, bitCount: Int): UByte = value.rotateRight(bitCount)
}

interface ShortBitRotate : BitRotate<Short> {
    override fun rotateLeft(value: Short, bitCount: Int): Short = value.rotateLeft(bitCount)
    override fun rotateRight(value: Short, bitCount: Int): Short = value.rotateRight(bitCount)
}

interface UShortBitRotate : BitRotate<UShort> {
    override fun rotateLeft(value: UShort, bitCount: Int): UShort = value.rotateLeft(bitCount)
    override fun rotateRight(value: UShort, bitCount: Int): UShort = value.rotateRight(bitCount)
}

interface IntBitRotate : BitRotate<Int> {
    override fun rotateLeft(value: Int, bitCount: Int): Int = value.rotateLeft(bitCount)
    override fun rotateRight(value: Int, bitCount: Int): Int = value.rotateRight(bitCount)
}

interface UIntBitRotate : BitRotate<UInt> {
    override fun rotateLeft(value: UInt, bitCount: Int): UInt = value.rotateLeft(bitCount)
    override fun rotateRight(value: UInt, bitCount: Int): UInt = value.rotateRight(bitCount)
}

interface LongBitRotate : BitRotate<Long> {
    override fun rotateLeft(value: Long, bitCount: Int): Long = value.rotateLeft(bitCount)
    override fun rotateRight(value: Long, bitCount: Int): Long = value.rotateRight(bitCount)
}

interface ULongBitRotate : BitRotate<ULong> {
    override fun rotateLeft(value: ULong, bitCount: Int): ULong = value.rotateLeft(bitCount)
    override fun rotateRight(value: ULong, bitCount: Int): ULong = value.rotateRight(bitCount)
}
