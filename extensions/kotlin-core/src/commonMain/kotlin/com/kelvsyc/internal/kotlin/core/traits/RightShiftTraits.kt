package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.RightShift

object ByteRightShift : RightShift<Byte> {
    override val sizeBits: Int by Byte.Companion::SIZE_BITS

    override fun rightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toByte()
        else (value.toUByte().toInt() ushr bitCount).toByte()
    }
}

object UByteRightShift : RightShift<UByte> {
    override val sizeBits: Int by Byte.Companion::SIZE_BITS

    override fun rightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUByte()
        else (value.toInt() shr bitCount).toUByte()
    }
}

object ShortRightShift : RightShift<Short> {
    override val sizeBits: Int by Short.Companion::SIZE_BITS

    override fun rightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toShort()
        else (value.toUShort().toInt() ushr bitCount).toShort()
    }
}

object UShortRightShift : RightShift<UShort> {
    override val sizeBits: Int by Short.Companion::SIZE_BITS

    override fun rightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUShort()
        else (value.toInt() shr bitCount).toUShort()
    }
}

object IntRightShift : RightShift<Int> {
    override val sizeBits: Int by Int.Companion::SIZE_BITS

    override fun rightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0
        else value ushr bitCount
    }
}

object UIntRightShift : RightShift<UInt> {
    override val sizeBits: Int by UInt.Companion::SIZE_BITS

    override fun rightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0U
        else value shr bitCount
    }
}

object LongRightShift : RightShift<Long> {
    override val sizeBits: Int by Long.Companion::SIZE_BITS

    override fun rightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0L
        else value ushr bitCount
    }
}

object ULongRightShift : RightShift<ULong> {
    override val sizeBits: Int by ULong.Companion::SIZE_BITS

    override fun rightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0UL
        else value shr bitCount
    }
}
