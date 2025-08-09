package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary16Sized

/**
 * Base implementation of a traits object for a `binary16` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary16Traits<T>(signed: Signed<T>) :
    AbstractFloatingPointTraits<T>(Binary16Sized),
    Signed<T> by signed,
    Sized by Binary16Sized,
    Binary16Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 10
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
