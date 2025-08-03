package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike
import com.kelvsyc.kotlin.core.traits.ArraySized

@OptIn(ExperimentalUnsignedTypes::class)
class UByteArrayBitCollection(sized: ArraySized<UByteArray, UByte>) : AbstractArrayBitCollection<UByteArray, UByte>(sized) {
    override val traits: ArrayLike<UByteArray, UByte> = TypeTraits.UByteArray
    override val base: BitCollection<UByte> = TypeTraits.UByte

    // These are overridden for efficiency
    override fun asByteArray(value: UByteArray): ByteArray = value.asByteArray()
}
