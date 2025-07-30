package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.ArithmeticRightShift
import com.kelvsyc.kotlin.core.traits.BitShift
import com.kelvsyc.kotlin.core.traits.LeftShift
import com.kelvsyc.kotlin.core.traits.RightShift

object ByteBitShift : BitShift<Byte>,
    LeftShift<Byte> by ByteLeftShift,
    RightShift<Byte> by ByteRightShift,
    ArithmeticRightShift<Byte> by ByteArithmeticRightShift

object UByteBitShift : BitShift<UByte>,
    LeftShift<UByte> by UByteLeftShift,
    RightShift<UByte> by UByteRightShift,
    ArithmeticRightShift<UByte> by UByteArithmeticRightShift

object ShortBitShift : BitShift<Short>,
    LeftShift<Short> by ShortLeftShift,
    RightShift<Short> by ShortRightShift,
    ArithmeticRightShift<Short> by ShortArithmeticRightShift

object UShortBitShift : BitShift<UShort>,
    LeftShift<UShort> by UShortLeftShift,
    RightShift<UShort> by UShortRightShift,
    ArithmeticRightShift<UShort> by UShortArithmeticRightShift

object IntBitShift : BitShift<Int>,
    LeftShift<Int> by IntLeftShift,
    RightShift<Int> by IntRightShift,
    ArithmeticRightShift<Int> by IntArithmeticRightShift

object UIntBitShift : BitShift<UInt>,
    LeftShift<UInt> by UIntLeftShift,
    RightShift<UInt> by UIntRightShift,
    ArithmeticRightShift<UInt> by UIntArithmeticRightShift

object LongBitShift : BitShift<Long>,
    LeftShift<Long> by LongLeftShift,
    RightShift<Long> by LongRightShift,
    ArithmeticRightShift<Long> by LongArithmeticRightShift

object ULongBitShift : BitShift<ULong>,
    LeftShift<ULong> by ULongLeftShift,
    RightShift<ULong> by ULongRightShift,
    ArithmeticRightShift<ULong> by ULongArithmeticRightShift

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
