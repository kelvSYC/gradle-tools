package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary64Sized

/**
 * Base implementation of a traits object for a `binary64` floating-point type (such as [Double]).
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary64Traits<T>(signed: Signed<T>, sized: Sized = Binary64Sized()) :
    AbstractFloatingPointTraits<T>(sized),
    Signed<T> by signed,
    Sized by sized,
    Binary64Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 52
    }

    init {
        require(sized.sizeBits == Binary64Sized.SIZE_BITS) { "Incorrect size specified for a binary64 type." }
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
