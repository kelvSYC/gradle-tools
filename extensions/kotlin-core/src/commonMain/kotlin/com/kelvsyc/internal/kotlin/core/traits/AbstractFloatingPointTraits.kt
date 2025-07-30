package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.FloatingPointTraits
import com.kelvsyc.kotlin.core.traits.Sized

abstract class AbstractFloatingPointTraits<T>(sized: Sized<T>) : FloatingPointTraits<T> {
    override val exponentWidth: Int by lazy { sized.sizeBits - mantissaWidth - 1 }
    override val precision: Int by lazy { mantissaWidth + 1 }
    override val exponentBias: Int by lazy { (1 shl exponentWidth - 1) - 1 }
    override val integralExponentBias: Int by lazy { exponentBias + mantissaWidth }
    override val exponentRange: IntRange by lazy { (1 - exponentBias) .. exponentBias }
    override val integralExponentRange: IntRange by lazy {
        (1 - exponentBias - mantissaWidth) .. (exponentBias - mantissaWidth)
    }
}
