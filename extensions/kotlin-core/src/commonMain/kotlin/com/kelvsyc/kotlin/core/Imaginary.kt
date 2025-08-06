package com.kelvsyc.kotlin.core

/**
 * Interface representing an imaginary number, backed by a floating-point type.
 *
 * The simplest implementation of this interface is a value class, but implementations are not required to implement
 * it as such.
 *
 * @param T The backing floating-point type
 */
interface Imaginary<T> {
    /**
     * Factory interface for the construction of [Imaginary] instances.
     *
     * @param I The imaginary type
     * @param T The backing floating-point type
     */
    interface Factory<T, I : Imaginary<T>> {
        /**
         * Creates a new imaginary value.
         */
        fun of(value: T): I
    }

    /**
     * Returns the magnitude of the imaginary number.
     */
    val value: T
}
