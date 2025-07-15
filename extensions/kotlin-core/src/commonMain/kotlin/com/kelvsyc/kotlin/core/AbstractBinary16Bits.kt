package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.AbstractShortBasedBitFieldDelegate

/**
 * Partial implementation of the bit representation of a `binary16` floating-point value.
 *
 * @param T the floating-point type
 */
abstract class AbstractBinary16Bits<T>(protected val bits: Short) : FloatingPointBits<T, Short> {
    companion object {
        /**
         * The number of bits in a `binary16` value.
         */
        const val SIZE_BITS = Short.SIZE_BITS

        /**
         * The number of bits in the significand of a `binary16` value.
         */
        const val PRECISION = 11

        /**
         * The number of bits in the exponent field of a `binary16 value.
         */
        const val EXPONENT_WIDTH = SIZE_BITS - PRECISION

        /**
         * The exponent bias of the `binary16` type.
         */
        const val EXPONENT_BIAS = (1 shl (EXPONENT_WIDTH - 1)) - 1

        /**
         * The range of unbiased exponents of the `binary16` type.
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
    override val biasedExponent by object : AbstractShortBasedBitFieldDelegate<Int>(
        this::bits, PRECISION - 1, EXPONENT_WIDTH
    ) {
        override val converter = Converter.of(Short::toInt, Int::toShort)
    }
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
