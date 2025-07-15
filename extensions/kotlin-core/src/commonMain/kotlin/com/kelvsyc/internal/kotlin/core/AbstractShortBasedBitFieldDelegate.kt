package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractBitFieldDelegate
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KProperty0

abstract class AbstractShortBasedBitFieldDelegate<T>(
    backingProperty: KProperty0<Short>, off: Int, len: Int
) : AbstractBitFieldDelegate<T, Short>(backingProperty, off, len) {
    override val bitShift
        get() = TypeTraits.Short
    override val bitwise
        get() = TypeTraits.Short

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Short {
        require(offset >= 0 && offset < Short.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Short.SIZE_BITS) { "Length must be in range" }

        var result = 0
        for (it in 0 ..< length) {
            result = result or (1 shl it)
        }
        return (result shl offset).toShort()
    }
}
