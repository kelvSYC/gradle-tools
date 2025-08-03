package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import com.kelvsyc.kotlin.core.traits.ArrayBitCollection
import com.kelvsyc.kotlin.core.traits.ArraySized

@OptIn(ExperimentalUnsignedTypes::class)
class UByteArrayBitCollection(sized: ArraySized<UByteArray, UByte>) :
    ArrayBitCollection<UByteArray, UByte>(sized, TypeTraits.UByteArray, TypeTraits.UByte) {

    // These are overridden for efficiency
    override fun asByteArray(value: UByteArray): ByteArray = value.asByteArray()
}
