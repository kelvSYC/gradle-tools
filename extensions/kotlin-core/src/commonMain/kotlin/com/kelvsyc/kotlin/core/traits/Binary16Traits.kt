package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to a `binary16` floating-point type.
 *
 * @param T The floating-point type.
 */
interface Binary16Traits<T> : FloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 16
        private const val MANTISSA_WIDTH = 10
    }

    override val sizeBits: Int
        get() = SIZE_BITS
    override val mantissaWidth: Int
        get() = MANTISSA_WIDTH
}
