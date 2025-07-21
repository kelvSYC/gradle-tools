package com.kelvsyc.kotlin.core.traits

/**
 * Traits relating to a `bfloat16` floating-point type.
 *
 * @param T The floating-point type
 */
interface BFloat16Traits<T> : FloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 16
        private const val MANTISSA_WIDTH = 7
    }

    override val sizeBits: Int
        get() = SIZE_BITS
    override val mantissaWidth: Int
        get() = MANTISSA_WIDTH
}
