package com.kelvsyc.kotlin.core

/**
 * Bit representation of a `binary32` floating-point value (ie. a [Float]).
 */
class FloatBits(bits: Int) : AbstractBinary32Bits<Float>(bits, TypeTraits.Float) {
    companion object {
        /**
         * [Converter] instance that can be used to convert a [Float] to a bit representation.
         */
        val converter = Converter.of(Float::toBits, Float.Companion::fromBits)
    }

    /**
     * Creates a [FloatBits] value from a [Float] value.
     */
    constructor(value: Float) : this(converter(value))

    override fun toFloatingPoint() = converter.reverse(bits)
}
