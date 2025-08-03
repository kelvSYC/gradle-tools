package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary256Sized

/**
 * Base implementation of a traits object for a `binary256` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary256Traits<T>(sized: Sized<T> = Binary256Sized()) :
    Sized<T> by sized,
    AbstractFloatingPointTraits<T>(Binary256Sized()) {
    companion object {
        private const val MANTISSA_WIDTH = 236
    }

    init {
        require(sized.sizeBits == Binary256Sized.SIZE_BITS) { "Incorrect size specified for a binary256 type." }
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
