package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary32Sized

/**
 * Base implementation of a traits object for a `binary32` floating-point type (such as [Float]).
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary32Traits<T>(signed: Signed<T>, sized: Sized<T> = Binary32Sized()) :
    AbstractFloatingPointTraits<T>(sized),
    Signed<T> by signed,
    Sized<T> by sized,
    Binary32Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 23
    }

    init {
        require(sized.sizeBits == Binary32Sized.SIZE_BITS) { "Incorrect size specified for a binary32 type." }
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
