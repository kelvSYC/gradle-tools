package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike
import com.kelvsyc.kotlin.core.traits.ArraySized

/**
 * Implementation of [BitCollection] on fixed-sized instances of [ByteArray], where bytes are arranged with the least
 * significant byte first.
 */
class ByteArrayBitCollection(sized: ArraySized<ByteArray, Byte>) : AbstractArrayBitCollection<ByteArray, Byte>(sized) {
    override val traits: ArrayLike<ByteArray, Byte> = TypeTraits.ByteArray
    override val base: BitCollection<Byte> = TypeTraits.Byte

    // These are overridden for efficiency
    override fun asByteArray(value: ByteArray): ByteArray = value
}
