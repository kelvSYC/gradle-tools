package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.AbstractArrayBitwise
import com.kelvsyc.kotlin.core.TypeTraits

class ByteArrayBitwise(size: Int? = null) : AbstractArrayBitwise<ByteArray, Byte>(
    size, TypeTraits.ByteArray, TypeTraits.Byte
) {
    override val zero: Byte = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class UByteArrayBitwise(size: Int? = null) : AbstractArrayBitwise<UByteArray, UByte>(
    size, TypeTraits.UByteArray, TypeTraits.UByte
) {
    override val zero: UByte = 0U
}

class ShortArrayBitwise(size: Int? = null) : AbstractArrayBitwise<ShortArray, Short>(
    size, TypeTraits.ShortArray, TypeTraits.Short
) {
    override val zero: Short = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class UShortArrayBitwise(size: Int? = null) : AbstractArrayBitwise<UShortArray, UShort>(
    size, TypeTraits.UShortArray, TypeTraits.UShort
) {
    override val zero: UShort = 0U
}

class IntArrayBitwise(size: Int? = null) : AbstractArrayBitwise<IntArray, Int>(
    size, TypeTraits.IntArray, TypeTraits.Int
) {
    override val zero: Int = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class UIntArrayBitwise(size: Int? = null) : AbstractArrayBitwise<UIntArray, UInt>(
    size, TypeTraits.UIntArray, TypeTraits.UInt
) {
    override val zero: UInt = 0U
}

class LongArrayBitwise(size: Int? = null) : AbstractArrayBitwise<LongArray, Long>(
    size, TypeTraits.LongArray, TypeTraits.Long
) {
    override val zero: Long = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class ULongArrayBitwise(size: Int? = null) : AbstractArrayBitwise<ULongArray, ULong>(
    size, TypeTraits.ULongArray, TypeTraits.ULong
) {
    override val zero: ULong = 0U
}
