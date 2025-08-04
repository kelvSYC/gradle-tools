package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Binary16Sized

/**
 * Base implementation of a traits object for a `binary16` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractBinary16Traits<T>(sized: Sized<T> = Binary16Sized()) : AbstractFloatingPointTraits<T>(sized),
    Sized<T> by sized,
    Binary16Traits<T> {
    companion object {
        private const val MANTISSA_WIDTH = 10
    }

    init {
        require(sized.sizeBits == Binary16Sized.SIZE_BITS) { "Incorrect size specified for a binary16 type." }
    }

    override val mantissaWidth: Int get() = MANTISSA_WIDTH
}
