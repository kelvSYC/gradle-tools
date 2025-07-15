package com.kelvsyc.kotlin.core

/**
 * Bit representation of a `binary32` floating-point value (ie. a [Float]).
 */
class FloatBits(private val bits: Int) : FloatingPointBits<Float, Int> {
    companion object {
        /**
         * The number of bits in a [Float] value.
         */
        const val SIZE_BITS = Float.SIZE_BITS

        /**
         * The number of bits in the significand of a [Float] value.
         */
        const val PRECISION = 24

        /**
         * The number of bits in the exponent field of a [Float] value.
         */
        const val EXPONENT_WIDTH = SIZE_BITS - PRECISION

        /**
         * The exponent bias of the [Float] type.
         */
        const val EXPONENT_BIAS = (1 shl (EXPONENT_WIDTH - 1)) - 1

        /**
         * The range of unbiased exponents of the [Float] type.
         */
        val EXPONENT_RANGE = (1 - EXPONENT_BIAS) ..EXPONENT_BIAS

        /**
         * The range of exponents if the significand were to be taken as an integral rather than a fixed-point value
         */
        val ADJUSTED_EXPONENT_RANGE = EXPONENT_RANGE.let {
            it.start - (PRECISION - 1) .. it.endInclusive - (PRECISION - 1)
        }

        /**
         * [Converter] instance that can be used to convert a [Float] to a bit representation.
         */
        val converter = Converter.of(Float::toBits, Float.Companion::fromBits)
    }

    /**
     * Creates a [FloatBits] value from a [Float] value.
     */
    constructor(value: Float) : this(converter(value))

    override val signBit by flag(this::bits, SIZE_BITS - 1)
    override val biasedExponent by bitfield(this::bits, PRECISION - 1, EXPONENT_WIDTH)
    override val mantissa by bitfield(this::bits, 0, PRECISION - 1)

    override val isNormal by lazy { biasedExponent != 0 && biasedExponent != (1 shl EXPONENT_WIDTH) - 1 }
    override val isZero by lazy { biasedExponent == 0 && mantissa == 0 }
    override val isSubnormal by lazy { biasedExponent == 0 && mantissa != 0 }
    override val isInfinite by lazy { biasedExponent == (1 shl EXPONENT_WIDTH) -1 && mantissa == 0 }
    override val isNaN by lazy { biasedExponent == (1 shl EXPONENT_WIDTH) -1 && mantissa != 0 }

    override val exponent by lazy { biasedExponent - EXPONENT_BIAS }
    override val significand by lazy {
        if (isNormal) {
            (1 shl PRECISION) or mantissa
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
        !signBit && isFinite && (significand and (significand - 1)) == 0
    }

    override fun toFloatingPoint() = converter.reverse(bits)
}
