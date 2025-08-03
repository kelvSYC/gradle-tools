package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractDecimalFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Decimal32Sized

/**
 * Base implementation of a traits object for a `decimal32` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractDecimal32Traits<T>(sized: Sized<T> = Decimal32Sized()) :
    Sized<T> by sized,
    AbstractDecimalFloatingPointTraits<T>(sized) {
    companion object {
        private const val CONTINUATION_WIDTH = 20
    }

    init {
        require(sized.sizeBits == Decimal32Sized.SIZE_BITS) { "Incorrect size specified for a decimal32 type." }
    }

    override val continuationWidth: Int get() = CONTINUATION_WIDTH
}
