package com.kelvsyc.kotlin.core.complex.traits

import com.kelvsyc.kotlin.core.Complex
import com.kelvsyc.kotlin.core.traits.FloatingPoint

/**
 * Basic implementation of [ComplexFloatingPoint].
 */
class BasicComplexFloatingPoint<T, C : Complex<T>>(
    private val baseTraits: FloatingPoint<T>,
    private val factory: Complex.Factory<T, C>
) : ComplexFloatingPoint<T, C> {
    override val zero: C by lazy { factory.ofCartesian(baseTraits.zero, baseTraits.zero) }
    override val one: C by lazy { factory.ofCartesian(baseTraits.one, baseTraits.zero) }
    override val i: C by lazy { factory.ofCartesian(baseTraits.zero, baseTraits.one) }

    override fun isInfinite(value: C): Boolean =
        baseTraits.isInfinite(value.real) || baseTraits.isInfinite(value.imaginary)

    override fun isNaN(value: C): Boolean =
        (baseTraits.isNaN(value.real) && !baseTraits.isInfinite(value.imaginary))
                || (baseTraits.isNaN(value.imaginary) && !baseTraits.isInfinite(value.real))

    override fun isFinite(value: C): Boolean = baseTraits.isFinite(value.real) && baseTraits.isFinite(value.imaginary)
}
