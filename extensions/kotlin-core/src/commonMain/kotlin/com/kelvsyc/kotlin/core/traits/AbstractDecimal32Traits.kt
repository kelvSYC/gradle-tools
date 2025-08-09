package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.AbstractDecimalFloatingPointTraits
import com.kelvsyc.internal.kotlin.core.traits.Decimal32Sized

/**
 * Base implementation of a traits object for a `decimal32` floating-point type.
 *
 * @param T The floating-point type.
 */
abstract class AbstractDecimal32Traits<T>(signed: Signed<T>) :
    AbstractDecimalFloatingPointTraits<T>(Decimal32Sized),
    Signed<T> by signed,
    Sized by Decimal32Sized,
    Decimal32Traits<T> {
    companion object {
        private const val CONTINUATION_WIDTH = 20
    }

    override val continuationWidth: Int get() = CONTINUATION_WIDTH
}
