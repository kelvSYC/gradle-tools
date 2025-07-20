package com.kelvsyc.kotlin.core

/**
 * Partial implementation of the bit representation of a `decimal32` floating-point value.
 *
 * @param T the floating-point type
 */
abstract class AbstractDecimal32Bits<T>(protected val bits: Int) : DecimalFloatingPointBits<T, Int> {
    companion object {
        /**
         * The number of bits in a `decimal32` value.
         */
        const val SIZE_BITS = Int.SIZE_BITS

        /**
         * The number of bits in the continuation field of a `decimal32` value.
         */
        const val CONTINUATION_WIDTH = 20

        /**
         * The number of bits in the combination field of a `decimal32` value.
         */
        const val COMBINATION_WIDTH = SIZE_BITS - CONTINUATION_WIDTH

        /**
         * The number of digits represented in the significand.
         */
        const val PRECISION = 7

        /**
         * The number of bits in the exponent of a `decimal32` value.
         */
        const val EXPONENT_BITS = COMBINATION_WIDTH - 3

        /**
         * The number of bits in the significand of a `decimal32` value.
         */
        const val SIGNIFICAND_BITS = CONTINUATION_WIDTH + 4

        /**
         * The exponent bias of the `decimal32` type, if the decimal point is placed after the first significant digit.
         */
        const val EXPONENT_BIAS = 96

        /**
         * The range of valid exponents of a `decimal32` value.
         */
        val exponentRange = (1 - EXPONENT_BIAS) .. EXPONENT_BIAS
    }

    protected enum class Discriminator {
        LOW, HIGH, INFINITE, NAN
    }

    override val signBit: Boolean by flag(this::bits, SIZE_BITS - 1)
    override val combination: Int by bitfield(this::bits, CONTINUATION_WIDTH, COMBINATION_WIDTH)
    override val continuation: Int by bitfield(this::bits, 0, CONTINUATION_WIDTH)

    private val topBits by bitfield(this::bits, SIZE_BITS - 6, 5)
    protected val discriminator by lazy {
        topBits.let {
            if (it and 0x18 != 0x18) Discriminator.LOW
            else if (it and 0x1E != 0x1E) Discriminator.HIGH
            else if (it == 0x1E) Discriminator.INFINITE
            else Discriminator.NAN
        }
    }

    private val signalling by flag(this::bits, SIZE_BITS - 7)
    override val isInfinite: Boolean by lazy { discriminator == Discriminator.INFINITE }
    override val isNaN: Boolean by lazy { discriminator == Discriminator.NAN }
    override val isQuietNaN: Boolean by lazy { isNaN && !signalling }
    override val isSignallingNaN: Boolean by lazy { isNaN && signalling }
}
