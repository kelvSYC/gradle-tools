package com.kelvsyc.kotlin.core

/**
 * Partial implementation of the bit representation of a `bfloat16` floating-point value.
 *
 * The `bfloat16` (Brain floating point 16-bit) value is a non-standard floating point format derived from `binary32`
 * (ie. [Float]) by truncating its significand so that it fits into a 16-bit value.
 *
 * @param T the floating-point type
 */
abstract class AbstractBFloat16Bits<T>(protected val bits: Short) : FloatingPointBits<T, Short> {
    companion object {
        /**
         * The number of bits in a `bfloat16` value.
         */
        const val SIZE_BITS = Short.SIZE_BITS

        /**
         * The number of bits in the significand of a `bfloat16` value.
         */
        const val PRECISION = 8

        /**
         * The number of bits in the exponent field of a `bfloat16` value.
         */
        const val EXPONENT_WIDTH = SIZE_BITS - PRECISION

        /**
         * The exponent bias of the `bfloat16` type.
         */
        const val EXPONENT_BIAS = (1 shl (EXPONENT_WIDTH - 1)) - 1

        /**
         * The range of unbiased exponents of the `bfloat16` type.
         */
        val EXPONENT_RANGE = (1 - EXPONENT_BIAS) ..EXPONENT_BIAS

        /**
         * The range of exponents if the significand were to be taken as an integral rather than a fixed-point value
         */
        val ADJUSTED_EXPONENT_RANGE = EXPONENT_RANGE.let {
            it.start - (PRECISION - 1) .. it.endInclusive - (PRECISION - 1)
        }
    }

    override val signBit by flag(this::bits, SIZE_BITS - 1)
    override val biasedExponent by bitfield(this::bits, PRECISION - 1, EXPONENT_WIDTH, Short::toInt)
    override val mantissa by bitfield(this::bits, 0, PRECISION - 1)

    override val isNormal by lazy { biasedExponent != 0 && biasedExponent != (1 shl EXPONENT_WIDTH) - 1 }
    override val isZero by lazy { biasedExponent == 0 && mantissa.toInt() == 0 }
    override val isSubnormal by lazy { biasedExponent == 0 && mantissa.toInt() != 0 }
    override val isInfinite by lazy { biasedExponent == (1 shl EXPONENT_WIDTH) -1 && mantissa.toInt() == 0 }
    override val isNaN by lazy { biasedExponent == (1 shl EXPONENT_WIDTH) -1 && mantissa.toInt() != 0 }

    override val exponent by lazy { biasedExponent - EXPONENT_BIAS }
    override val significand by lazy {
        if (isNormal) {
            ((1 shl PRECISION) or mantissa.toInt()).toShort()
        } else if (isSubnormal) {
            (mantissa.toInt() shl 1).toShort()
        } else {
            mantissa
        }
    }

    override val isMathematicalInteger by lazy {
        isFinite && (isZero || PRECISION - 1 - mantissa.countTrailingZeroBits() <= exponent)
    }
    override val isPowerOfTwo by lazy {
        !signBit && isFinite && significand.toInt().let { it and (it - 1) } == 0
    }
}
