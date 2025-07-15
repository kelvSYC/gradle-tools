package com.kelvsyc.kotlin.core

/**
 * Bit representation of a `binary16` floating-point value (ie. a [Float16]).
 */
class Float16Bits private constructor(bits: Short) : AbstractBinary16Bits<Float16>(bits) {
    companion object {
        val converter = Converter.of(Float16::bits, ::Float16)

        /**
         * Creates a [Float16Bits] from a raw representation of the floating-point value.
         */
        fun ofBits(bits: Short) = Float16Bits(bits)

        /**
         * Creates a [Float16Bits] value from a [Float16] value.
         */
        fun ofValue(value: Float16) = Float16Bits(value.bits)
    }

    override fun toFloatingPoint(): Float16 = converter.reverse(bits)
}
