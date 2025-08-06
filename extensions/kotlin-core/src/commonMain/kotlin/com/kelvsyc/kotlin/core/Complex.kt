package com.kelvsyc.kotlin.core

/**
 * Interface representing a complex number, whose real and imaginary parts are backed by a floating-point type.
 *
 * Note that despite there being separate types for [imaginary][Imaginary] and complex numbers, the two types are
 * nominally unrelated to each other except as a means of exchanging data. Implementations are free to use or not use
 * [Imaginary] in its implementation of this interface.
 *
 * @param T the floating-point type backing the real and imaginary parts.
 */
interface Complex<T> {
    /**
     * Factory interface for the creation of [Complex] instances.
     *
     * @param C The complex number type
     * @param T The backing floating-point type
     */
    interface Factory<T, C : Complex<T>> {
        /**
         * Creates a new [Complex] instance from the specified real and imaginary parts.
         *
         * The behavior as it relates to passing in non-normal (infinite or `NaN`) values for the real or imaginary
         * parts is undefined.
         */
        fun ofCartesian(real: T, imaginary: T): C

        /**
         * Creates a new [Complex] instance from the specified real and imaginary parts.
         *
         * The behavior as it relates to passing in non-normal (infinite or `NaN`) values for the real or imaginary
         * parts is undefined.
         */
        fun ofCartesian(real: T, imaginary: Imaginary<T>): C

        /**
         * Creates a new [Complex] instance with real part zero and the specified imaginary part.
         *
         * The behavior as it relates to passing in non-normal (infinite or `NaN`) imaginary part is undefined.
         */
        fun ofImaginary(imaginary: Imaginary<T>): C
    }

    /**
     * Returns the real part of this number.
     */
    val real : T

    /**
     * Returns the imaginary part of this number.
     */
    val imaginary : T
}
