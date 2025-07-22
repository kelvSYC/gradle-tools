package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractMutableBitFieldDelegate
import com.kelvsyc.kotlin.core.Converter
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KMutableProperty0

abstract class AbstractMutableLongBasedBitFieldDelegate<S, T>(
    backingProperty: KMutableProperty0<S>,
    off: Int,
    len: Int,
    converter: Converter<Long, T>
) : AbstractMutableBitFieldDelegate<S, T, Long>(backingProperty, off, len, converter) {
    override val bitShift
        get() = TypeTraits.Long
    override val bitwise
        get() = TypeTraits.Long

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Long {
        require(offset >= 0 && offset < Long.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Long.SIZE_BITS) { "Length must be in range" }

        val result = (1L shl length) - 1
        return result shl offset
    }
}
