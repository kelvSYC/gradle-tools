package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import com.kelvsyc.kotlin.core.traits.ArrayBitCollection
import com.kelvsyc.kotlin.core.traits.ArraySized

/**
 * Implementation of [BitCollection] on fixed-sized instances of [ByteArray], where bytes are arranged with the least
 * significant byte first.
 */
class ByteArrayBitCollection(sized: ArraySized<ByteArray, Byte>) :
    ArrayBitCollection<ByteArray, Byte>(sized, TypeTraits.ByteArray, TypeTraits.Byte) {
    // These are overridden for efficiency
    override fun asByteArray(value: ByteArray): ByteArray = value
}
