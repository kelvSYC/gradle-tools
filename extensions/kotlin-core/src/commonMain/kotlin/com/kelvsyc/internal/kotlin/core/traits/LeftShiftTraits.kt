package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.LeftShift

object ByteLeftShift : LeftShift<Byte> {
    override val sizeBits: Int by Byte.Companion::SIZE_BITS

    override fun leftShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toByte()
        else (value.toInt() shl bitCount).toByte()
    }
}

object UByteLeftShift : LeftShift<UByte> {
    override val sizeBits: Int by Byte.Companion::SIZE_BITS

    override fun leftShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUByte()
        else (value.toInt() shl bitCount).toUByte()
    }
}

object ShortLeftShift : LeftShift<Short> {
    override val sizeBits: Int by Short.Companion::SIZE_BITS

    override fun leftShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toShort()
        else (value.toInt() shl bitCount).toShort()
    }
}

object UShortLeftShift : LeftShift<UShort> {
    override val sizeBits: Int by Short.Companion::SIZE_BITS

    override fun leftShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0.toUShort()
        else (value.toInt() shl bitCount).toUShort()
    }
}

object IntLeftShift : LeftShift<Int> {
    override val sizeBits: Int by Int.Companion::SIZE_BITS

    override fun leftShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0
        else value shl bitCount
    }
}

object UIntLeftShift : LeftShift<UInt> {
    override val sizeBits: Int by UInt.Companion::SIZE_BITS

    override fun leftShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0U
        else value shl bitCount
    }
}

object LongLeftShift : LeftShift<Long> {
    override val sizeBits: Int by Long.Companion::SIZE_BITS

    override fun leftShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0L
        else value shl bitCount
    }
}

object ULongLeftShift : LeftShift<ULong> {
    override val sizeBits: Int by ULong.Companion::SIZE_BITS

    override fun leftShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= sizeBits) 0UL
        else value shl bitCount
    }
}
