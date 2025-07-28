package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.BitShift

interface ByteBitShift : BitShift<Byte>,
    ByteSized,
    ByteLeftShift,
    ByteRightShift,
    ByteArithmeticRightShift {
    override val sizeBits: Int get() = super<ByteSized>.sizeBits
    override fun leftShift(value: Byte, bitCount: Int): Byte = super.leftShift(value, bitCount)
    override fun rightShift(value: Byte, bitCount: Int): Byte = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: Byte, bitCount: Int): Byte = super.arithmeticRightShift(value, bitCount)
}

interface UByteBitShift : BitShift<UByte>,
    UByteSized,
    UByteLeftShift,
    UByteRightShift,
    UByteArithmeticRightShift {
    override val sizeBits: Int get() = super<UByteSized>.sizeBits
    override fun leftShift(value: UByte, bitCount: Int): UByte = super.leftShift(value, bitCount)
    override fun rightShift(value: UByte, bitCount: Int): UByte = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: UByte, bitCount: Int): UByte = super.arithmeticRightShift(value, bitCount)
}

interface ShortBitShift : BitShift<Short>,
    ShortSized,
    ShortLeftShift,
    ShortRightShift,
    ShortArithmeticRightShift {
    override val sizeBits: Int get() = super<ShortSized>.sizeBits
    override fun leftShift(value: Short, bitCount: Int): Short = super.leftShift(value, bitCount)
    override fun rightShift(value: Short, bitCount: Int): Short = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: Short, bitCount: Int): Short = super.arithmeticRightShift(value, bitCount)
}

interface UShortBitShift : BitShift<UShort>,
    UShortSized,
    UShortLeftShift,
    UShortRightShift,
    UShortArithmeticRightShift {
    override val sizeBits: Int get() = super<UShortSized>.sizeBits
    override fun leftShift(value: UShort, bitCount: Int): UShort = super.leftShift(value, bitCount)
    override fun rightShift(value: UShort, bitCount: Int): UShort = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: UShort, bitCount: Int): UShort = super.arithmeticRightShift(value, bitCount)
}

interface IntBitShift : BitShift<Int>,
    IntSized,
    IntLeftShift,
    IntRightShift,
    IntArithmeticRightShift {
    override val sizeBits: Int get() = super<IntSized>.sizeBits
    override fun leftShift(value: Int, bitCount: Int): Int = super.leftShift(value, bitCount)
    override fun rightShift(value: Int, bitCount: Int): Int = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: Int, bitCount: Int): Int = super.arithmeticRightShift(value, bitCount)
}

interface UIntBitShift : BitShift<UInt>,
    UIntSized,
    UIntLeftShift,
    UIntRightShift,
    UIntArithmeticRightShift {
    override val sizeBits: Int get() = super<UIntSized>.sizeBits
    override fun leftShift(value: UInt, bitCount: Int): UInt = super.leftShift(value, bitCount)
    override fun rightShift(value: UInt, bitCount: Int): UInt = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: UInt, bitCount: Int): UInt = super.arithmeticRightShift(value, bitCount)
}

interface LongBitShift : BitShift<Long>,
    LongSized,
    LongLeftShift,
    LongRightShift,
    LongArithmeticRightShift {
    override val sizeBits: Int get() = super<LongSized>.sizeBits
    override fun leftShift(value: Long, bitCount: Int): Long = super.leftShift(value, bitCount)
    override fun rightShift(value: Long, bitCount: Int): Long = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: Long, bitCount: Int): Long = super.arithmeticRightShift(value, bitCount)
}

interface ULongBitShift : BitShift<ULong>,
    ULongSized,
    ULongLeftShift,
    ULongRightShift,
    ULongArithmeticRightShift {
    override val sizeBits: Int get() = super<ULongSized>.sizeBits
    override fun leftShift(value: ULong, bitCount: Int): ULong = super.leftShift(value, bitCount)
    override fun rightShift(value: ULong, bitCount: Int): ULong = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: ULong, bitCount: Int): ULong = super.arithmeticRightShift(value, bitCount)
}

interface ByteArrayBitShift : BitShift<ByteArray>,
    ByteArrayLeftShift,
    ByteArrayRightShift,
    ByteArrayArithmeticRightShift {
    override fun leftShift(value: ByteArray, bitCount: Int): ByteArray = super.leftShift(value, bitCount)
    override fun rightShift(value: ByteArray, bitCount: Int): ByteArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: ByteArray, bitCount: Int): ByteArray =
        super.arithmeticRightShift(value, bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UByteArrayBitShift : BitShift<UByteArray>,
    UByteArrayLeftShift,
    UByteArrayRightShift,
    UByteArrayArithmeticRightShift {
    override fun leftShift(value: UByteArray, bitCount: Int): UByteArray = super.leftShift(value, bitCount)
    override fun rightShift(value: UByteArray, bitCount: Int): UByteArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: UByteArray, bitCount: Int): UByteArray =
        super.arithmeticRightShift(value, bitCount)
}

interface ShortArrayBitShift : BitShift<ShortArray>,
    ShortArrayLeftShift,
    ShortArrayRightShift,
    ShortArrayArithmeticRightShift {
    override fun leftShift(value: ShortArray, bitCount: Int): ShortArray = super.leftShift(value, bitCount)
    override fun rightShift(value: ShortArray, bitCount: Int): ShortArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: ShortArray, bitCount: Int): ShortArray =
        super.arithmeticRightShift(value, bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UShortArrayBitShift : BitShift<UShortArray>,
    UShortArrayLeftShift,
    UShortArrayRightShift,
    UShortArrayArithmeticRightShift {
    override fun leftShift(value: UShortArray, bitCount: Int): UShortArray = super.leftShift(value, bitCount)
    override fun rightShift(value: UShortArray, bitCount: Int): UShortArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: UShortArray, bitCount: Int): UShortArray =
        super.arithmeticRightShift(value, bitCount)
}

interface IntArrayBitShift : BitShift<IntArray>,
    IntArrayLeftShift,
    IntArrayRightShift,
    IntArrayArithmeticRightShift {
    override fun leftShift(value: IntArray, bitCount: Int): IntArray = super.leftShift(value, bitCount)
    override fun rightShift(value: IntArray, bitCount: Int): IntArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: IntArray, bitCount: Int): IntArray =
        super.arithmeticRightShift(value, bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UIntArrayBitShift : BitShift<UIntArray>,
    UIntArrayLeftShift,
    UIntArrayRightShift,
    UIntArrayArithmeticRightShift {
    override fun leftShift(value: UIntArray, bitCount: Int): UIntArray = super.leftShift(value, bitCount)
    override fun rightShift(value: UIntArray, bitCount: Int): UIntArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: UIntArray, bitCount: Int): UIntArray =
        super.arithmeticRightShift(value, bitCount)
}

interface LongArrayBitShift : BitShift<LongArray>,
    LongArrayLeftShift,
    LongArrayRightShift,
    LongArrayArithmeticRightShift {
    override fun leftShift(value: LongArray, bitCount: Int): LongArray = super.leftShift(value, bitCount)
    override fun rightShift(value: LongArray, bitCount: Int): LongArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: LongArray, bitCount: Int): LongArray =
        super.arithmeticRightShift(value, bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface ULongArrayBitShift : BitShift<ULongArray>,
    ULongArrayLeftShift,
    ULongArrayRightShift,
    ULongArrayArithmeticRightShift {
    override fun leftShift(value: ULongArray, bitCount: Int): ULongArray = super.leftShift(value, bitCount)
    override fun rightShift(value: ULongArray, bitCount: Int): ULongArray = super.rightShift(value, bitCount)
    override fun arithmeticRightShift(value: ULongArray, bitCount: Int): ULongArray =
        super.arithmeticRightShift(value, bitCount)
}
