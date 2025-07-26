package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.ArithmeticRightShift

interface ByteArithmeticRightShift : ArithmeticRightShift<Byte>, ByteSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toByte()
        else (value.toInt() shr bitCount).toByte()
    }
}

interface UByteArithmeticRightShift : ArithmeticRightShift<UByte>, UByteSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUByte()
        else (value.toByte().toInt() shr bitCount).toUByte()
    }
}

interface ShortArithmeticRightShift : ArithmeticRightShift<Short>, ShortSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toShort()
        else (value.toInt() shr bitCount).toShort()
    }
}

interface UShortArithmeticRightShift : ArithmeticRightShift<UShort>, UShortSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUShort()
        else (value.toShort().toInt() shr bitCount).toUShort()
    }
}

interface IntArithmeticRightShift : ArithmeticRightShift<Int>, IntSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0
        else value shr bitCount
    }
}

interface UIntArithmeticRightShift : ArithmeticRightShift<UInt>, UIntSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0U
        else (value.toInt() shr bitCount).toUInt()
    }
}

interface LongArithmeticRightShift : ArithmeticRightShift<Long>, LongSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0L
        else value shr bitCount
    }
}

interface ULongArithmeticRightShift : ArithmeticRightShift<ULong>, ULongSized {
    override val sizeBits: Int
        get() = super.sizeBits

    override fun arithmeticRightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0UL
        else (value.toLong() shr bitCount).toULong()
    }
}
