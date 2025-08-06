package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.Signed

/**
 * Implementation of [Signed] for [Imaginary], given by the [Signed] implementation of their backing floating-point
 * type.
 *
 * The implementation of [Signed] is based on the magnitude of the imaginary value.
 *
 * @param I The imaginary type
 * @param T The backing floating-point type
 */
class ImaginarySigned<T, I : Imaginary<T>>(
    private val baseSigned: Signed<T>,
    private val factory: Imaginary.Factory<T, I>
) : Signed<I> {
    override fun isPositive(value: I): Boolean = baseSigned.isPositive(value.value)
    override fun isNegative(value: I): Boolean = baseSigned.isNegative(value.value)
    override fun negate(value: I): I = factory.of(baseSigned.negate(value.value))
    override fun absoluteValue(value: I): I = factory.of(baseSigned.negate(value.value))
}
