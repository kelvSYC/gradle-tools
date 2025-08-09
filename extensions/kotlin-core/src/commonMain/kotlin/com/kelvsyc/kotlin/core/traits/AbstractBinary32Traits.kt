package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary32Sized

/**
 * Base implementation of a traits object for a `binary32` floating-point type (such as [Float]).
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary32Traits<T>(signed: Signed<T>) :
    AbstractFloatingPointTraits<T>(Binary32Sized),
    Signed<T> by signed,
    Sized by Binary32Sized,
    Binary32Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 23
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
