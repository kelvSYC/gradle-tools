package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Imaginary
import com.kelvsyc.kotlin.core.traits.FloatingPointTraits

/**
 * Basic implementation of [ImaginaryFloatingPoint].
 */
class BasicImaginaryFloatingPoint<T, I : Imaginary<T>>(
    private val baseTraits: FloatingPointTraits<T>,
    private val factory: Imaginary.Factory<T, I>
) : ImaginaryFloatingPoint<T, I> {
    override val zero: I by lazy { factory.of(baseTraits.zero) }
    override val i: I by lazy { factory.of(baseTraits.one) }

    override fun isInfinite(value: I): Boolean = baseTraits.isInfinite(value.value)
    override fun isNaN(value: I): Boolean = baseTraits.isNaN(value.value)
    override fun isFinite(value: I): Boolean = baseTraits.isFinite(value.value)
}
