package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractDecimalFloatingPointTraits

/**
 * Base implementation of a traits object for a `decimal128` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractDecimal128Traits<T>(sized: Sized<T>) :
    Sized<T> by sized,
    AbstractDecimalFloatingPointTraits<T>(sized) {
    companion object {
        private const val CONTINUATION_WIDTH = 100
    }

    override val continuationWidth: Int get() = CONTINUATION_WIDTH
}
