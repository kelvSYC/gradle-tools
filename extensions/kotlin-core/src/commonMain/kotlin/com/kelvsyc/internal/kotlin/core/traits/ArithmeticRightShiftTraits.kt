package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.ArithmeticRightShift

object ByteArithmeticRightShift : ArithmeticRightShift<Byte> {
    override val sizeBits: Int by Byte.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toByte()
        else (value.toInt() shr bitCount).toByte()
    }
}

object UByteArithmeticRightShift : ArithmeticRightShift<UByte> {
    override val sizeBits: Int by UByte.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUByte()
        else (value.toByte().toInt() shr bitCount).toUByte()
    }
}

object ShortArithmeticRightShift : ArithmeticRightShift<Short> {
    override val sizeBits: Int by Short.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toShort()
        else (value.toInt() shr bitCount).toShort()
    }
}

object UShortArithmeticRightShift : ArithmeticRightShift<UShort> {
    override val sizeBits: Int by UShort.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUShort()
        else (value.toShort().toInt() shr bitCount).toUShort()
    }
}

object IntArithmeticRightShift : ArithmeticRightShift<Int> {
    override val sizeBits: Int by Int.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0
        else value shr bitCount
    }
}

object UIntArithmeticRightShift : ArithmeticRightShift<UInt> {
    override val sizeBits: Int by UInt.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0U
        else (value.toInt() shr bitCount).toUInt()
    }
}

object LongArithmeticRightShift : ArithmeticRightShift<Long> {
    override val sizeBits: Int by Long.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0L
        else value shr bitCount
    }
}

object ULongArithmeticRightShift : ArithmeticRightShift<ULong> {
    override val sizeBits: Int by ULong.Companion::SIZE_BITS

    override fun arithmeticRightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0UL
        else (value.toLong() shr bitCount).toULong()
    }
}
