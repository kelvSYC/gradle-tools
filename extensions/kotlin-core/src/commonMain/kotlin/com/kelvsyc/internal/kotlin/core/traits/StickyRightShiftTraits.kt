package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.StickyRightShift

object ByteStickyRightShift : StickyRightShift<Byte> {
    override fun stickyRightShift(value: Byte, bitCount: Int): Byte = if (bitCount >= Byte.SIZE_BITS) {
        if (value.toInt() == 0) 0 else 1
    } else {
        val mask = (1 shl bitCount) - 1
        val sticky = if (value.toInt() and mask == 0) 0 else 1
        ((value.toUByte().toInt() ushr bitCount) or sticky).toByte()
    }
}

object UByteStickyRightShift : StickyRightShift<UByte> {
    override fun stickyRightShift(value: UByte, bitCount: Int): UByte = if (bitCount >= UByte.SIZE_BITS) {
        if (value.toInt() == 0) 0U else 1U
    } else {
        val mask = (1 shl bitCount) - 1
        val sticky = if (value.toInt() and mask == 0) 0 else 1
        ((value.toUInt().toInt() ushr bitCount) or sticky).toUByte()
    }
}

object ShortStickyRightShift : StickyRightShift<Short> {
    override fun stickyRightShift(value: Short, bitCount: Int): Short = if (bitCount >= Short.SIZE_BITS) {
        if (value.toInt() == 0) 0 else 1
    } else {
        val mask = (1 shl bitCount) - 1
        val sticky = if (value.toInt() and mask == 0) 0 else 1
        ((value.toUShort().toInt() ushr bitCount) or sticky).toShort()
    }
}

object UShortStickyRightShift : StickyRightShift<UShort> {
    override fun stickyRightShift(value: UShort, bitCount: Int): UShort = if (bitCount >= UShort.SIZE_BITS) {
        if (value.toInt() == 0) 0U else 1U
    } else {
        val mask = (1 shl bitCount) - 1
        val sticky = if (value.toInt() and mask == 0) 0 else 1
        ((value.toUInt().toInt() ushr bitCount) or sticky).toUShort()
    }
}

object IntStickyRightShift : StickyRightShift<Int> {
    override fun stickyRightShift(value: Int, bitCount: Int): Int = if (bitCount >= Int.SIZE_BITS) {
        if (value == 0) 0 else 1
    } else {
        val mask = (1 shl bitCount) - 1
        val sticky = if (value and mask == 0) 0 else 1
        (value ushr bitCount) or sticky
    }
}

object UIntStickyRightShift : StickyRightShift<UInt> {
    override fun stickyRightShift(value: UInt, bitCount: Int): UInt = if (bitCount >= UInt.SIZE_BITS) {
        if (value == 0U) 0U else 1U
    } else {
        val mask = (1U shl bitCount) - 1U
        val sticky = if (value and mask == 0U) 0U else 1U
        (value shr bitCount) or sticky
    }
}

object LongStickyRightShift : StickyRightShift<Long> {
    override fun stickyRightShift(value: Long, bitCount: Int): Long = if (bitCount >= Long.SIZE_BITS) {
        if (value == 0L) 0 else 1
    } else {
        val mask = (1L shl bitCount) - 1
        val sticky = if (value and mask == 0L) 0L else 1L
        (value ushr bitCount) or sticky
    }
}

object ULongStickyRightShift : StickyRightShift<ULong> {
    override fun stickyRightShift(value: ULong, bitCount: Int): ULong = if (bitCount >= ULong.SIZE_BITS) {
        if (value == 0UL) 0U else 1U
    } else {
        val mask = (1UL shl bitCount) - 1U
        val sticky = if (value and mask == 0UL) 0UL else 1UL
        (value shr bitCount) or sticky
    }
}
