package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.BFloat16Sized

/**
 * Base implementation of a traits object for a `bfloat16` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBFloat16Traits<T>(signed: Signed<T>) :
    AbstractFloatingPointTraits<T>(BFloat16Sized),
    Signed<T> by signed,
    Sized by BFloat16Sized,
    BFloat16Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 7
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
