package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.AbstractArrayBitwise
import com.kelvsyc.kotlin.core.Bitwise
import com.kelvsyc.kotlin.core.TypeTraits
import com.kelvsyc.kotlin.core.traits.ArrayLike

class ByteArrayBitwise(size: Int? = null) : AbstractArrayBitwise<ByteArray, Byte>(size) {
    override val traits: ArrayLike<ByteArray, Byte> = TypeTraits.ByteArray
    override val base: Bitwise<Byte> = TypeTraits.Byte
    override val zero: Byte = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class UByteArrayBitwise(size: Int? = null) : AbstractArrayBitwise<UByteArray, UByte>(size) {
    override val traits: ArrayLike<UByteArray, UByte> = TypeTraits.UByteArray
    override val base: Bitwise<UByte> = TypeTraits.UByte
    override val zero: UByte = 0U
}

class ShortArrayBitwise(size: Int? = null) : AbstractArrayBitwise<ShortArray, Short>(size) {
    override val traits: ArrayLike<ShortArray, Short> = TypeTraits.ShortArray
    override val base: Bitwise<Short> = TypeTraits.Short
    override val zero: Short = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class UShortArrayBitwise(size: Int? = null) : AbstractArrayBitwise<UShortArray, UShort>(size) {
    override val traits: ArrayLike<UShortArray, UShort> = TypeTraits.UShortArray
    override val base: Bitwise<UShort> = TypeTraits.UShort
    override val zero: UShort = 0U
}

class IntArrayBitwise(size: Int? = null) : AbstractArrayBitwise<IntArray, Int>(size) {
    override val traits: ArrayLike<IntArray, Int> = TypeTraits.IntArray
    override val base: Bitwise<Int> = TypeTraits.Int
    override val zero: Int = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class UIntArrayBitwise(size: Int? = null) : AbstractArrayBitwise<UIntArray, UInt>(size) {
    override val traits: ArrayLike<UIntArray, UInt> = TypeTraits.UIntArray
    override val base: Bitwise<UInt> = TypeTraits.UInt
    override val zero: UInt = 0U
}

class LongArrayBitwise(size: Int? = null) : AbstractArrayBitwise<LongArray, Long>(size) {
    override val traits: ArrayLike<LongArray, Long> = TypeTraits.LongArray
    override val base: Bitwise<Long> = TypeTraits.Long
    override val zero: Long = 0
}

@OptIn(ExperimentalUnsignedTypes::class)
class ULongArrayBitwise(size: Int? = null) : AbstractArrayBitwise<ULongArray, ULong>(size) {
    override val traits: ArrayLike<ULongArray, ULong> = TypeTraits.ULongArray
    override val base: Bitwise<ULong> = TypeTraits.ULong
    override val zero: ULong = 0U
}
