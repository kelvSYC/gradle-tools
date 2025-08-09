package com.kelvsyc.kotlin.core.traits

/**
 * Traits denoting that a type has a fixed size in terms of number of bits.
 */
interface Sized {
    /**
     * The number of bits the type takes up.
     */
    val sizeBits: Int

    /**
     * The number of bytes the type takes up.
     */
    val sizeBytes: Int
        get() = sizeBits / Byte.SIZE_BITS
}
