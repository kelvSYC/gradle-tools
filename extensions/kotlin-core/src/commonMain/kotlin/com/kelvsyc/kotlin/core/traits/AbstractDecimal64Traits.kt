package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractDecimalFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Decimal64Sized

/**
 * Base implementation of a traits object for a `decimal64` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractDecimal64Traits<T>(signed: Signed<T>) :
    AbstractDecimalFloatingPointTraits<T>(Decimal64Sized),
    Signed<T> by signed,
    Sized by Decimal64Sized,
    Decimal64Traits<T> {
    companion object {
        private const val CONTINUATION_WIDTH = 50
    }

    override val continuationWidth: Int get() = CONTINUATION_WIDTH
}
