package com.kelvsyc.kotlin.core.traits

/**
 * Traits relevant to a `binary32` floating-point value (such as a [Float]).
 *
 * @param T The floating-point type.
 */
interface Binary32Traits<T> : FloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 32
        private const val MANTISSA_WIDTH = 23
    }

    override val sizeBits: Int
        get() = SIZE_BITS
    override val mantissaWidth: Int
        get() = MANTISSA_WIDTH
}
