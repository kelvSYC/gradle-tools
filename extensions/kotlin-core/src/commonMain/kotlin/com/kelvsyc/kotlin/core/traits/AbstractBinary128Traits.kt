package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary128Sized

/**
 * Base implementation of a traits object for a `binary128` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary128Traits<T>(signed: Signed<T>) :
    AbstractFloatingPointTraits<T>(Binary128Sized),
    Signed<T> by signed,
    Sized by Binary128Sized,
    Binary128Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 112
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
