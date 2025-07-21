package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to a `binary64` floating-point type (such as a [Double]).
 *
 * @param T The floating-point type.
 */
interface Binary64Traits<T> : FloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 64
        private const val MANTISSA_WIDTH = 52
    }

    override val sizeBits: Int
        get() = SIZE_BITS
    override val mantissaWidth: Int
        get() = MANTISSA_WIDTH
}
