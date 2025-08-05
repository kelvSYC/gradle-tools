package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary128Sized

/**
 * Base implementation of a traits object for a `binary128` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary128Traits<T>(signed: Signed<T>, sized: Sized<T> = Binary128Sized()) :
    AbstractFloatingPointTraits<T>(sized),
    Signed<T> by signed,
    Sized<T> by sized,
    Binary128Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 112
    }

    init {
        require(sized.sizeBits == Binary128Sized.SIZE_BITS) { "Incorrect size specified for a binary128 type." }
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
