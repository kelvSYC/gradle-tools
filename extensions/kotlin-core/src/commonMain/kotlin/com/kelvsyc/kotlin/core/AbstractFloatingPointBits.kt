package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.FloatingPointTraits

/**
 * Partial representation of the bit representation of a binary floating-point value.
 *
 * @param T The floating-point type.
 */
abstract class AbstractFloatingPointBits<T, B>(
    protected val bits: B, protected open val traits: FloatingPointTraits<T>
) : FloatingPointBits<T, B> {
    override val isNormal by lazy { biasedExponent != 0 && biasedExponent != (1 shl traits.exponentWidth) - 1 }

    override val exponent by lazy { biasedExponent - traits.exponentBias }
    override val integralExponent: Int by lazy { biasedExponent - traits.integralExponentBias }
}
