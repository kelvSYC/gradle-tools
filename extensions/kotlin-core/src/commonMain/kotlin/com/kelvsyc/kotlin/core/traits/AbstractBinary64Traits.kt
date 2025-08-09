package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary64Sized

/**
 * Base implementation of a traits object for a `binary64` floating-point type (such as [Double]).
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary64Traits<T>(signed: Signed<T>) :
    AbstractFloatingPointTraits<T>(Binary64Sized),
    Signed<T> by signed,
    Sized by Binary64Sized,
    Binary64Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 52
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
