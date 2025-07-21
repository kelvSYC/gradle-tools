package com.kelvsyc.kotlin.core.traits

interface Binary256Traits<T> : FloatingPointTraits<T> {
    companion object {
        private const val SIZE_BITS = 256
        private const val MANTISSA_WIDTH = 236
    }

    override val sizeBits: Int
        get() = SIZE_BITS
    override val mantissaWidth: Int
        get() = MANTISSA_WIDTH
}
