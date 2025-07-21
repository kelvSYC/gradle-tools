package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to `decimal64` floating-point types.
 *
 * @param T The floating-point type
 */
interface Decimal64Traits<T> : DecimalFloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 64
        private const val CONTINUATION_WIDTH = 50
    }
    override val sizeBits: Int
        get() = SIZE_BITS
    override val continuationWidth: Int
        get() = CONTINUATION_WIDTH
}
