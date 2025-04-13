package com.kelvsyc.kotlin.math

/**
 * Canonical [DoubleStore] implementation, where the backing store is a [LongBitStore].
 */
@JvmInline
value class CanonicalDoubleStore private constructor(override val bits: LongBitStore) : DoubleStore<LongBitStore, Long> {
    companion object : DoubleStore.AbstractCompanion<CanonicalDoubleStore, LongBitStore, Long>() {
        override val traits = LongBitStore
        override val rawToInt = Long::toInt

        override fun create(raw: LongBitStore) = CanonicalDoubleStore(raw)
        override fun create(value: Double) = CanonicalDoubleStore(LongBitStore(value.toRawBits()))
    }

    override val isNegative: Boolean
        get() = bits[sizeBits - 1]

    override val biasedExponent: Int
        get() {
            val raw = (bits and exponentMask) shr (precision - 1)
            return raw.bits.toInt()
        }

    override val rawSignificand: LongBitStore
        get() = (bits and significandMask)

    override val exponent: Int
        get() = biasedExponent - exponentBias

    override val significand: LongBitStore
        get() = rawSignificand.let {
            if (biasedExponent == 0) {
                it
            } else {
                it or hiddenBit
            }
        }

    override val isSubNormal: Boolean
        get() = biasedExponent == 0 && significand != traits.zero
    override val isZero: Boolean
        get() = biasedExponent == 0 && significand == traits.zero
    override val isFinite: Boolean
        get() = exponent <= maxExponent
    override val isNormal: Boolean
        get() = exponent >= minExponent
    override val isInfinity: Boolean
        get() = exponent == exponentBias + 1 && significand == traits.zero
    override val isNaN: Boolean
        get() = exponent == exponentBias + 1 && significand != traits.zero

    override fun toDouble() = Double.fromBits(bits.bits)
}
