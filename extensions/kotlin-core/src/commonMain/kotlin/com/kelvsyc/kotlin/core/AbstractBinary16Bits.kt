package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.AbstractBinary16Traits

/**
 * Partial implementation of the bit representation of a `binary16` floating-point value.
 *
 * @param T the floating-point type
 */
abstract class AbstractBinary16Bits<T>(
    bits: Short,
    override val traits: AbstractBinary16Traits<T>
) : AbstractFloatingPointBits<T, Short>(bits, traits) {
    override val signBit by flag(this::bits, traits.sizeBits - 1)
    override val biasedExponent by bitfield(this::bits, traits.mantissaWidth, traits.exponentWidth, TypeTraits.Short, Short::toInt)
    override val mantissa by bitfield(this::bits, 0, traits.mantissaWidth)

    override val isZero by lazy { biasedExponent == 0 && mantissa.toInt() == 0 }
    override val isSubnormal by lazy { biasedExponent == 0 && mantissa.toInt() != 0 }
    override val isInfinite by lazy { biasedExponent == (1 shl traits.exponentWidth) - 1 && mantissa.toInt() == 0 }
    override val isNaN by lazy { biasedExponent == (1 shl traits.exponentWidth) - 1 && mantissa.toInt() != 0 }

    override val significand by lazy {
        if (isNormal) {
            ((1 shl traits.precision) or mantissa.toInt()).toShort()
        } else if (isSubnormal) {
            (mantissa.toInt() shl 1).toShort()
        } else {
            mantissa
        }
    }

    override val isMathematicalInteger by lazy {
        isFinite && (isZero || traits.mantissaWidth - mantissa.countTrailingZeroBits() <= exponent)
    }
    override val isPowerOfTwo by lazy {
        !signBit && isFinite && significand.toInt().let { it and (it - 1) } == 0
    }
}
