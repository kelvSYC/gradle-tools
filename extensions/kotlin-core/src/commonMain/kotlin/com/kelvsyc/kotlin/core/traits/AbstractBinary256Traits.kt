package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary256Sized

/**
 * Base implementation of a traits object for a `binary256` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary256Traits<T>(signed: Signed<T>) :
    AbstractFloatingPointTraits<T>(Binary256Sized),
    Signed<T> by signed,
    Sized by Binary256Sized,
    Binary256Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 236
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
