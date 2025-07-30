package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.ArithmeticRightShift
import com.kelvsyc.kotlin.core.traits.BitShift
import com.kelvsyc.kotlin.core.traits.LeftShift
import com.kelvsyc.kotlin.core.traits.RightShift

object ByteBitShift : BitShift<Byte>,
    ByteSized,
    LeftShift<Byte> by ByteLeftShift,
    RightShift<Byte> by ByteRightShift,
    ArithmeticRightShift<Byte> by ByteArithmeticRightShift {
    override val sizeBits: Int get() = super<ByteSized>.sizeBits
}

object UByteBitShift : BitShift<UByte>,
    UByteSized,
    LeftShift<UByte> by UByteLeftShift,
    RightShift<UByte> by UByteRightShift,
    ArithmeticRightShift<UByte> by UByteArithmeticRightShift {
    override val sizeBits: Int get() = super<UByteSized>.sizeBits
}

object ShortBitShift : BitShift<Short>,
    ShortSized,
    LeftShift<Short> by ShortLeftShift,
    RightShift<Short> by ShortRightShift,
    ArithmeticRightShift<Short> by ShortArithmeticRightShift {
    override val sizeBits: Int get() = super<ShortSized>.sizeBits
}

object UShortBitShift : BitShift<UShort>,
    UShortSized,
    LeftShift<UShort> by UShortLeftShift,
    RightShift<UShort> by UShortRightShift,
    ArithmeticRightShift<UShort> by UShortArithmeticRightShift {
    override val sizeBits: Int get() = super<UShortSized>.sizeBits
}

object IntBitShift : BitShift<Int>,
    IntSized,
    LeftShift<Int> by IntLeftShift,
    RightShift<Int> by IntRightShift,
    ArithmeticRightShift<Int> by IntArithmeticRightShift {
    override val sizeBits: Int get() = super<IntSized>.sizeBits
}

object UIntBitShift : BitShift<UInt>,
    UIntSized,
    LeftShift<UInt> by UIntLeftShift,
    RightShift<UInt> by UIntRightShift,
    ArithmeticRightShift<UInt> by UIntArithmeticRightShift {
    override val sizeBits: Int get() = super<UIntSized>.sizeBits
}

object LongBitShift : BitShift<Long>,
    LongSized,
    LeftShift<Long> by LongLeftShift,
    RightShift<Long> by LongRightShift,
    ArithmeticRightShift<Long> by LongArithmeticRightShift {
    override val sizeBits: Int get() = super<LongSized>.sizeBits
}

object ULongBitShift : BitShift<ULong>,
    ULongSized,
    LeftShift<ULong> by ULongLeftShift,
    RightShift<ULong> by ULongRightShift,
    ArithmeticRightShift<ULong> by ULongArithmeticRightShift {
    override val sizeBits: Int get() = super<ULongSized>.sizeBits
}

object ByteArrayBitShift : BitShift<ByteArray>,
    LeftShift<ByteArray> by ByteArrayLeftShift,
    RightShift<ByteArray> by ByteArrayRightShift,
    ArithmeticRightShift<ByteArray> by ByteArrayArithmeticRightShift

@OptIn(ExperimentalUnsignedTypes::class)
object UByteArrayBitShift : BitShift<UByteArray>,
    LeftShift<UByteArray> by UByteArrayLeftShift,
    RightShift<UByteArray> by UByteArrayRightShift,
    ArithmeticRightShift<UByteArray> by UByteArrayArithmeticRightShift

object ShortArrayBitShift : BitShift<ShortArray>,
    LeftShift<ShortArray> by ShortArrayLeftShift,
    RightShift<ShortArray> by ShortArrayRightShift,
    ArithmeticRightShift<ShortArray> by ShortArrayArithmeticRightShift

@OptIn(ExperimentalUnsignedTypes::class)
object UShortArrayBitShift : BitShift<UShortArray>,
    LeftShift<UShortArray> by UShortArrayLeftShift,
    RightShift<UShortArray> by UShortArrayRightShift,
    ArithmeticRightShift<UShortArray> by UShortArrayArithmeticRightShift

object IntArrayBitShift : BitShift<IntArray>,
    LeftShift<IntArray> by IntArrayLeftShift,
    RightShift<IntArray> by IntArrayRightShift,
    ArithmeticRightShift<IntArray> by IntArrayArithmeticRightShift

@OptIn(ExperimentalUnsignedTypes::class)
object UIntArrayBitShift : BitShift<UIntArray>,
    LeftShift<UIntArray> by UIntArrayLeftShift,
    RightShift<UIntArray> by UIntArrayRightShift,
    ArithmeticRightShift<UIntArray> by UIntArrayArithmeticRightShift

object LongArrayBitShift : BitShift<LongArray>,
    LeftShift<LongArray> by LongArrayLeftShift,
    RightShift<LongArray> by LongArrayRightShift,
    ArithmeticRightShift<LongArray> by LongArrayArithmeticRightShift

@OptIn(ExperimentalUnsignedTypes::class)
object ULongArrayBitShift : BitShift<ULongArray>,
    LeftShift<ULongArray> by ULongArrayLeftShift,
    RightShift<ULongArray> by ULongArrayRightShift,
    ArithmeticRightShift<ULongArray> by ULongArrayArithmeticRightShift
