package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.BFloat16Sized

/**
 * Base implementation of a traits object for a `bfloat16` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBFloat16Traits<T>(sized: Sized<T> = BFloat16Sized()) :
    Sized<T> by sized,
    AbstractFloatingPointTraits<T>(sized) {
    companion object {
        private const val MANTISSA_WIDTH = 7
    }

    init {
        require(sized.sizeBits == BFloat16Sized.SIZE_BITS) { "Incorrect size specified for a bfloat16 type." }
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
