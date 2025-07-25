package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike

@OptIn(ExperimentalUnsignedTypes::class)
class UByteArrayBitCollection(size: Int) : AbstractArrayBitCollection<UByteArray, UByte>(size) {
    override val traits: ArrayLike<UByteArray, UByte> = TypeTraits.UByteArray
    override val base: BitCollection<UByte> = TypeTraits.UByte

    // These are overridden for efficiency
    override val sizeBits: Int = size * UByte.SIZE_BITS
    override fun asByteArray(value: UByteArray): ByteArray = value.asByteArray()
    override fun isZero(value: UByteArray): Boolean = value.all { it.toInt() == 0 }
}
