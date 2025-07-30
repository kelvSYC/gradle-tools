package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractDecimalFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Decimal64Sized

/**
 * Base implementation of a traits object for a `decimal64` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractDecimal64Traits<T>(sized: Sized<T> = Decimal64Sized()) :
    Sized<T> by sized,
    AbstractDecimalFloatingPointTraits<T>(sized) {
    companion object {
        private const val CONTINUATION_WIDTH = 50
    }

    override val continuationWidth: Int get() = CONTINUATION_WIDTH
}
