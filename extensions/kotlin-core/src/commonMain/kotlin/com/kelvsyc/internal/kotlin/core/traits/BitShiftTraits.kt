package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.BitShift
import com.kelvsyc.kotlin.core.traits.ArithmeticRightShift
import com.kelvsyc.kotlin.core.traits.LeftShift
import com.kelvsyc.kotlin.core.traits.RightShift

object ByteBitShift : BitShift<Byte>,
    LeftShift<Byte> by ByteLeftShift,
    RightShift<Byte> by ByteRightShift,
    ArithmeticRightShift<Byte> by ByteArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by Byte.Companion::SIZE_BITS
}

object UByteBitShift : BitShift<UByte>,
    LeftShift<UByte> by UByteLeftShift,
    RightShift<UByte> by UByteRightShift,
    ArithmeticRightShift<UByte> by UByteArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by UByte.Companion::SIZE_BITS
}

object ShortBitShift : BitShift<Short>,
    LeftShift<Short> by ShortLeftShift,
    RightShift<Short> by ShortRightShift,
    ArithmeticRightShift<Short> by ShortArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by Short.Companion::SIZE_BITS
}

object UShortBitShift : BitShift<UShort>,
    LeftShift<UShort> by UShortLeftShift,
    RightShift<UShort> by UShortRightShift,
    ArithmeticRightShift<UShort> by UShortArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by UShort.Companion::SIZE_BITS
}

object IntBitShift : BitShift<Int>,
    LeftShift<Int> by IntLeftShift,
    RightShift<Int> by IntRightShift,
    ArithmeticRightShift<Int> by IntArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by Int.Companion::SIZE_BITS
}

object UIntBitShift : BitShift<UInt>,
    LeftShift<UInt> by UIntLeftShift,
    RightShift<UInt> by UIntRightShift,
    ArithmeticRightShift<UInt> by UIntArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by UInt.Companion::SIZE_BITS
}

object LongBitShift : BitShift<Long>,
    LeftShift<Long> by LongLeftShift,
    RightShift<Long> by LongRightShift,
    ArithmeticRightShift<Long> by LongArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by Long.Companion::SIZE_BITS
}

object ULongBitShift : BitShift<ULong>,
    LeftShift<ULong> by ULongLeftShift,
    RightShift<ULong> by ULongRightShift,
    ArithmeticRightShift<ULong> by ULongArithmeticRightShift {
    // Multiple interfaces define it, so we override explicitly
    override val sizeBits: Int by ULong.Companion::SIZE_BITS
}
