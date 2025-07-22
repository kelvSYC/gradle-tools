package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractMutableBitFieldDelegate
import com.kelvsyc.kotlin.core.Converter
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KMutableProperty0

abstract class AbstractMutableByteBasedBitFieldDelegate<S, T>(
    backingProperty: KMutableProperty0<S>,
    off: Int,
    len: Int,
    converter: Converter<Byte, T>
) : AbstractMutableBitFieldDelegate<S, T, Byte>(backingProperty, off, len, converter) {
    override val bitShift
        get() = TypeTraits.Byte
    override val bitwise
        get() = TypeTraits.Byte

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Byte {
        require(offset >= 0 && offset < Byte.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Byte.SIZE_BITS) { "Length must be in range" }

        val result = (1 shl length) - 1
        return (result shl offset).toByte()
    }
}
