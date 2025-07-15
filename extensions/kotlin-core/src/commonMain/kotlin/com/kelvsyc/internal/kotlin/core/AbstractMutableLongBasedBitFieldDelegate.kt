package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractMutableBitFieldDelegate
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KMutableProperty0

abstract class AbstractMutableLongBasedBitFieldDelegate<T>(
    backingProperty: KMutableProperty0<Long>, off: Int, len: Int
) : AbstractMutableBitFieldDelegate<T, Long>(backingProperty, off, len) {
    override val bitShift
        get() = TypeTraits.Long
    override val bitwise
        get() = TypeTraits.Long

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Long {
        require(offset >= 0 && offset < Long.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Long.SIZE_BITS) { "Length must be in range" }

        var result = 0L
        for (it in 0 ..< length) {
            result = result or (1L shl it)
        }
        return result shl offset
    }
}
