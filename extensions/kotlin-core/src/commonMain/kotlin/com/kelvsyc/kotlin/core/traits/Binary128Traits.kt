package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to a `binary128` floating-point type
 *
 * @param T The floating-point type
 */
interface Binary128Traits<T> : FloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 128
        private const val MANTISSA_WIDTH = 112
    }

    override val sizeBits: Int
        get() = SIZE_BITS
    override val mantissaWidth: Int
        get() = MANTISSA_WIDTH
}
