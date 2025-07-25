package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike

/**
 * Implementation of [BitCollection] on fixed-sized instances of [ByteArray], where bytes are arranged with the least
 * significant byte first.
 */
class ByteArrayBitCollection(size: Int) : AbstractArrayBitCollection<ByteArray, Byte>(size) {
    override val traits: ArrayLike<ByteArray, Byte> = TypeTraits.ByteArray
    override val base: BitCollection<Byte> = TypeTraits.Byte

    // These are overridden for efficiency
    override val sizeBits: Int = size * Byte.SIZE_BITS
    override fun asByteArray(value: ByteArray): ByteArray = value
    override fun isZero(value: ByteArray): Boolean = value.all { it.toInt() == 0 }
}
