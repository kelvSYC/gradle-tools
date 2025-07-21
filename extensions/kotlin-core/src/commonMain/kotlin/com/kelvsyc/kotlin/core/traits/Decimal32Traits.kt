package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to `decimal32` floating-point types.
 *
 * @param T The floating-point type
 */
interface Decimal32Traits<T> : DecimalFloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 32
        private const val CONTINUATION_WIDTH = 20
    }
    override val sizeBits: Int
        get() = SIZE_BITS
    override val continuationWidth: Int
        get() = CONTINUATION_WIDTH
}
