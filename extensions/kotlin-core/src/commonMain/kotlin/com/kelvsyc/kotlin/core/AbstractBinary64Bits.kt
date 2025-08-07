package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.Binary64Traits

/**
 * Partial implementation of the bit representation of a `binary64` floating-point value.
 *
 * @param T the floating-point type
 */
abstract class AbstractBinary64Bits<T>(
    bits: Long,
    override val traits: Binary64Traits<T>
) : AbstractFloatingPointBits<T, Long>(bits, traits, TypeTraits.Long) {
    override val signBit by flag(this::bits, traits.sizeBits - 1)
    override val biasedExponent by bitfield(this::bits, traits.mantissaWidth, traits.exponentWidth, TypeTraits.Long, Long::toInt)
    override val mantissa by bitfield(this::bits, 0, traits.mantissaWidth)

    override val significand by lazy {
        if (isNormal) {
            ((1L shl traits.precision) or mantissa)
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
        !signBit && isFinite && significand.let { it and (it - 1) } == 0L
    }
}
