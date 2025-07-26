package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.RightShift

interface ByteRightShift : RightShift<Byte>, ByteSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toByte()
        else (value.toUByte().toInt() ushr bitCount).toByte()
    }
}

interface UByteRightShift : RightShift<UByte>, UByteSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUByte()
        else (value.toInt() shr bitCount).toUByte()
    }
}

interface ShortRightShift : RightShift<Short>, ShortSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toShort()
        else (value.toUShort().toInt() ushr bitCount).toShort()
    }
}

interface UShortRightShift : RightShift<UShort>, UShortSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUShort()
        else (value.toInt() shr bitCount).toUShort()
    }
}

interface IntRightShift : RightShift<Int>, IntSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0
        else value ushr bitCount
    }
}

interface UIntRightShift : RightShift<UInt>, UIntSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0U
        else value shr bitCount
    }
}

interface LongRightShift : RightShift<Long>, LongSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0L
        else value ushr bitCount
    }
}

interface ULongRightShift : RightShift<ULong>, ULongSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun rightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0UL
        else value shr bitCount
    }
}
