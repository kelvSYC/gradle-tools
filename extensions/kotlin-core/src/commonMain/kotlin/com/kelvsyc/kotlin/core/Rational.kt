package com.kelvsyc.kotlin.core

/**
 * Interface representing a rational number, with numerators and denominators backed by an integral type.
 *
 * Implementations are not required to represent a rational number in its reduced form.
 *
 * If the underlying integral type is signed, so is this rational type. In this case, there is no requirement that sign
 * information be stored within either the numerator or denominator.
 *
 * @param T The integral type backing the numerator and denominator.
 */
interface Rational<T> {
    /**
     * Factory interface for the creation of [Rational] instances.
     *
     * @param R The rational number type
     * @param T The backing integral type
     */
    interface Factory<T, R : Rational<T>> {
        /**
         * Creates a new [Rational] instance using the supplied numerator and denominator.
         *
         * Implementations are not required to store the numerator or denominator as-is - that is, the returned
         * [numerator][Rational.numerator] may not equal the supplied [numerator]. In other words, implementations are free
         * to store the returned rational number in its reduced form, for example.
         *
         * @throws ArithmeticException if [denominator] is zero
         */
        fun create(numerator: T, denominator: T): R
    }

    /**
     * Returns the numerator of this number.
     */
    val numerator: T

    /**
     * Returns the denominator of this number. This value is always nonzero.
     */
    val denominator: T

    /**
     * Returns a rational number numerically equivalent to this value, with the numerator and denominator in its reduced
     * form - that is, a form where `gcd(`[numerator][numerator]`, `[denominator]`) == 1`.
     *
     * If [numerator] is zero, then the [denominator] of the returned value is set to 1.
     *
     * Implementations are generally expected to return an object of the same type.
     */
    val reduced: Rational<T>
}
