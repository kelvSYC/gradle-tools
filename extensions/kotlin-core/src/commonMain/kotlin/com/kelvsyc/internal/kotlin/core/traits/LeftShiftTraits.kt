package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.LeftShift

interface ByteLeftShift : LeftShift<Byte>, ByteSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toByte()
        else (value.toInt() shl bitCount).toByte()
    }
}

interface UByteLeftShift : LeftShift<UByte>, UByteSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUByte()
        else (value.toInt() shl bitCount).toUByte()
    }
}

interface ShortLeftShift : LeftShift<Short>, ShortSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toShort()
        else (value.toInt() shl bitCount).toShort()
    }
}

interface UShortLeftShift : LeftShift<UShort>, UShortSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUShort()
        else (value.toInt() shl bitCount).toUShort()
    }
}

interface IntLeftShift : LeftShift<Int>, IntSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0
        else value shl bitCount
    }
}

interface UIntLeftShift : LeftShift<UInt>, UIntSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0U
        else value shl bitCount
    }
}

interface LongLeftShift : LeftShift<Long>, LongSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0L
        else value shl bitCount
    }
}

interface ULongLeftShift : LeftShift<ULong>, ULongSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun leftShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0UL
        else value shl bitCount
    }
}
