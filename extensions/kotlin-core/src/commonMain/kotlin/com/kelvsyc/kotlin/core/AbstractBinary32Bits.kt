package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.AbstractBinary32Traits

/**
 * Partial implementation of the bit representation of a `binary32` floating-point value.
 *
 * @param T the floating-point type
 */
abstract class AbstractBinary32Bits<T>(
    bits: Int,
    override val traits: AbstractBinary32Traits<T>
) : AbstractFloatingPointBits<T, Int>(bits, traits) {
    override val signBit by flag(this::bits, traits.sizeBits - 1)
    override val biasedExponent by bitfield(this::bits, traits.mantissaWidth, traits.exponentWidth)
    override val mantissa by bitfield(this::bits, 0, traits.mantissaWidth)

    override val isZero by lazy { biasedExponent == 0 && mantissa == 0 }
    override val isSubnormal by lazy { biasedExponent == 0 && mantissa != 0 }
    override val isInfinite by lazy { biasedExponent == (1 shl traits.exponentWidth) - 1 && mantissa == 0 }
    override val isNaN by lazy { biasedExponent == (1 shl traits.exponentWidth) - 1 && mantissa != 0 }

    override val significand by lazy {
        if (isNormal) {
            ((1 shl traits.precision) or mantissa)
        } else if (isSubnormal) {
            (mantissa shl 1)
        } else {
            mantissa
        }
    }

    override val isMathematicalInteger by lazy {
        isFinite && (isZero || traits.mantissaWidth - mantissa.countTrailingZeroBits() <= exponent)
    }
    override val isPowerOfTwo by lazy {
        !signBit && isFinite && significand.let { it and (it - 1) } == 0
    }
}
