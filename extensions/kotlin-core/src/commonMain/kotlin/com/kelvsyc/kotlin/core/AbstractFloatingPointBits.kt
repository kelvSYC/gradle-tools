package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.FloatingPointTraits
import com.kelvsyc.kotlin.core.traits.IntegralConstants

/**
 * Partial representation of the bit representation of a binary floating-point value.
 *
 * @param T The floating-point type.
 */
abstract class AbstractFloatingPointBits<T, B>(
    protected val bits: B,
    protected open val traits: FloatingPointTraits<T>,
    protected val integralTraits: IntegralConstants<B>
) : FloatingPointBits<T, B> {
    override val isNormal: Boolean by lazy { biasedExponent != 0 && biasedExponent != (1 shl traits.exponentWidth) - 1 }

    override val exponent: Int by lazy { biasedExponent - traits.exponentBias }
    override val integralExponent: Int by lazy { biasedExponent - traits.integralExponentBias }

    override val isZero: Boolean by lazy { biasedExponent == 0 && integralTraits.isZero(mantissa) }
    override val isSubnormal: Boolean by lazy { biasedExponent == 0 && !integralTraits.isZero(mantissa) }
    override val isInfinite: Boolean by lazy {
        biasedExponent == (1 shl traits.exponentWidth) - 1 && integralTraits.isZero(mantissa)
    }
    override val isNaN: Boolean by lazy {
        biasedExponent == (1 shl traits.exponentWidth) - 1 && !integralTraits.isZero(mantissa)
    }
}
