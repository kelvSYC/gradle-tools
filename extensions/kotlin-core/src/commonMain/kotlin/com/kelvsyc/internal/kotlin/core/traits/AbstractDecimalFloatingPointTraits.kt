package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.DecimalFloatingPointTraits
import com.kelvsyc.kotlin.core.traits.Sized

abstract class AbstractDecimalFloatingPointTraits<T>(sized: Sized) : DecimalFloatingPointTraits<T> {
    override val combinationWidth: Int by lazy { sized.sizeBits - continuationWidth }
    override val precision: Int by lazy { (continuationWidth / 10) * 3 + 1 }
    override val exponentBits: Int by lazy { combinationWidth - 1 }
    override val significandBits: Int by lazy { continuationWidth + 4 }
    override val exponentBias: Int by lazy { (1 shl (exponentBits - 2)) * 3 }
    override val integralExponentBias: Int by lazy { exponentBias + precision }
    override val exponentRange: IntRange by lazy { (1 - exponentBias) .. exponentBias }
    override val integralExponentRange: IntRange by lazy { (1 - exponentBias - precision) .. (exponentBias - precision)}
}
