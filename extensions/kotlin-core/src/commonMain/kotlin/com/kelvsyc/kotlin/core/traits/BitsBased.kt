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
     * Returns a [Converter] that converts between a type and its bit representation.
     */
    val converter: Converter<T, B>
}
