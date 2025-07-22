package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractBitFieldDelegate
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KProperty0

abstract class AbstractShortBasedBitFieldDelegate<S, T>(
    backingProperty: KProperty0<S>, off: Int, len: Int
) : AbstractBitFieldDelegate<S, T, Short>(backingProperty, off, len) {
    override val bitShift
        get() = TypeTraits.Short
    override val bitwise
        get() = TypeTraits.Short

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Short {
        require(offset >= 0 && offset < Short.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Short.SIZE_BITS) { "Length must be in range" }

        val result = (1 shl length) - 1
        return (result shl offset).toShort()
    }
}
