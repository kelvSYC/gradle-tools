package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to `decimal128` floating-point types.
 *
 * @param T The floating-point type
 */
interface Decimal128Traits<T> : DecimalFloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 128
        private const val CONTINUATION_WIDTH = 110
    }
    override val sizeBits: Int
        get() = SIZE_BITS
    override val continuationWidth: Int
        get() = CONTINUATION_WIDTH
}
