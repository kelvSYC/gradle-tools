package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractDecimalFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Decimal128Sized

/**
 * Base implementation of a traits object for a `decimal128` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractDecimal128Traits<T>(signed: Signed<T>, sized: Sized = Decimal128Sized()) :
    AbstractDecimalFloatingPointTraits<T>(sized),
    Signed<T> by signed,
    Sized by sized,
    Decimal128Traits<T> {
    companion object {
        private const val CONTINUATION_WIDTH = 100
    }

    override val continuationWidth: Int get() = CONTINUATION_WIDTH
}
