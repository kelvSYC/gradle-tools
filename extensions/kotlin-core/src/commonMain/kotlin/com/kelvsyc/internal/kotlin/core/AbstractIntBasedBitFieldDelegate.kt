package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractBitFieldDelegate
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KProperty0

abstract class AbstractIntBasedBitFieldDelegate<S, T>(
    backingProperty: KProperty0<S>, off: Int, len: Int
) : AbstractBitFieldDelegate<S, T, Int>(backingProperty, off, len) {
    override val bitShift
        get() = TypeTraits.Int
    override val bitwise
        get() = TypeTraits.Int

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Int {
        require(offset >= 0 && offset < Int.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Int.SIZE_BITS) { "Length must be in range" }

        val result = (1 shl length) - 1
        return result shl offset
    }
}
