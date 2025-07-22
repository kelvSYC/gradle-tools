package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.Converter

/**
 * Type trait denoting that a type is backed by a bit store.
 *
 * @param T The type in question
 * @param B The backing bit store
 */
interface BitsBased<T, B> {
    /**
     * Returns the size of the type, in bits. This should be of a size that can be held by the backing bit store.
     */
    val sizeBits: Int

    /**
     * Returns a [Converter] that converts between a type and its bit representation.
     */
    val converter: Converter<T, B>
}
