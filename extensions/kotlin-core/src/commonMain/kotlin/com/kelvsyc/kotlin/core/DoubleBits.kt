package com.kelvsyc.kotlin.core

/**
 * Bit representation of a `binary64` floating-point value (ie. a [Double]).
 */
class DoubleBits(bits: Long) : AbstractBinary64Bits<Double>(bits, TypeTraits.Double) {
    companion object {
        /**
         * [Converter] instance that can be used to convert a [Float] to a bit representation.
         */
        val converter = Converter.of(Double::toBits, Double.Companion::fromBits)
    }

    /**
     * Creates a [DoubleBits] value from a [Double] value.
     */
    constructor(value: Double) : this(converter(value))

    override fun toFloatingPoint() = converter.reverse(bits)
}
