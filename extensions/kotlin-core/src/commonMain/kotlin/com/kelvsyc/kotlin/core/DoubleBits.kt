package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.AbstractLongBasedBitFieldDelegate

/**
 * Bit representation of a `binary64` floating-point value (ie. a [Double]).
 */
class DoubleBits(private val bits: Long) : FloatingPointBits<Double, Long> {
    companion object {
        /**
         * The number of bits in a [Double] value.
         */
        const val SIZE_BITS = Double.SIZE_BITS

        /**
         * The number of bits in the significand of a [Double] value.
         */
        const val PRECISION = 53

        /**
         * The number of bits in the exponent field of a [Double] value.
         */
        const val EXPONENT_WIDTH = SIZE_BITS - PRECISION

        /**
         * The exponent bias of the [Double] type.
         */
        const val EXPONENT_BIAS = (1 shl (EXPONENT_WIDTH - 1)) - 1

        /**
         * The range of unbiased exponents of the [Double] type.
         */
        val EXPONENT_RANGE = (1 - EXPONENT_BIAS) .. EXPONENT_BIAS

        /**
         * The range of exponents if the significand were to be taken as an integral rather than a fixed-point value
         */
        val ADJUSTED_EXPONENT_RANGE = EXPONENT_RANGE.let {
            it.start - (PRECISION - 1) .. it.endInclusive - (PRECISION - 1)
        }

        /**
         * [Converter] instance that can be used to convert a [Float] to a bit representation.
         */
        val converter = Converter.of(Double::toBits, Double.Companion::fromBits)
    }

    /**
     * Creates a [DoubleBits] value from a [Double] value.
     */
    constructor(value: Double) : this(converter(value))

    override val signBit by flag(this::bits, SIZE_BITS - 1)
    override val biasedExponent by object : AbstractLongBasedBitFieldDelegate<Int>(
        this::bits, PRECISION - 1, EXPONENT_WIDTH) {
        override val converter = Converter.of(Long::toInt, Int::toLong)
    }
    override val mantissa by bitfield(this::bits, 0, PRECISION - 1)

    override val isNormal by lazy { biasedExponent != 0 && biasedExponent != (1 shl EXPONENT_WIDTH) - 1 }
    override val isZero by lazy { biasedExponent == 0 && mantissa == 0L }
    override val isSubnormal by lazy { biasedExponent == 0 && mantissa != 0L }
    override val isInfinite by lazy { biasedExponent == (1 shl EXPONENT_WIDTH) -1 && mantissa == 0L }
    override val isNaN by lazy { biasedExponent == (1 shl EXPONENT_WIDTH) -1 && mantissa != 0L }

    override val exponent by lazy { biasedExponent - EXPONENT_BIAS }
    override val significand by lazy {
        if (isNormal) {
            (1L shl PRECISION) or mantissa
        } else if (isSubnormal) {
            mantissa shl 1
        } else {
            mantissa
        }
    }

    override val isMathematicalInteger by lazy {
        isFinite && (isZero || PRECISION - 1 - mantissa.countTrailingZeroBits() <= exponent)
    }
    override val isPowerOfTwo by lazy {
        !signBit && isFinite && (significand and (significand - 1)) == 0L
    }

    override fun toFloatingPoint() = converter.reverse(bits)
}
