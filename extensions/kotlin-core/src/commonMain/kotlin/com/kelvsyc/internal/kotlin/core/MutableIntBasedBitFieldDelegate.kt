package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.AbstractMutableBitFieldDelegate
import com.kelvsyc.kotlin.core.Converter
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.reflect.KMutableProperty0

class MutableIntBasedBitFieldDelegate<T>(
    backingProperty: KMutableProperty0<Int>,
    off: Int,
    len: Int,
    converter: Converter<Int, T>
) : AbstractMutableBitFieldDelegate<T, Int>(backingProperty, off, len, converter) {
    override val bitShift
        get() = TypeTraits.Int
    override val bitwise
        get() = TypeTraits.Int

    @OptIn(ExperimentalStdlibApi::class)
    override fun getMask(offset: Int, length: Int): Int {
        require(offset >= 0 && offset < Int.SIZE_BITS) { "Offset must be in range" }
        require(length > 0 && offset + length <= Int.SIZE_BITS) { "Length must be in range" }

        var result = 0
        for (it in 0 ..< length) {
            result = result or (1 shl it)
        }
        return result shl offset
    }
}
