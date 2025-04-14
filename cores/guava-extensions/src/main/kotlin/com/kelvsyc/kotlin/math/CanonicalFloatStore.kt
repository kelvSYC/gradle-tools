package com.kelvsyc.kotlin.math

import com.kelvsyc.kotlin.guava.math.isPowerOfTwo

/**
 * Canonical [FloatStore] implementation, where the backing store is an [IntBitStore].
 */
@JvmInline
value class CanonicalFloatStore private constructor(override val bits: IntBitStore) : FloatStore<IntBitStore, Int> {
    companion object : FloatStore.AbstractCompanion<CanonicalFloatStore, IntBitStore, Int>() {
        override val traits = IntBitStore
        override val rawToInt: (Int) -> Int = { it }
        override val rawIsZero: (Int) -> Boolean = { it == 0 }

        override fun create(raw: IntBitStore) = CanonicalFloatStore(raw)
        override fun create(value: Float) = CanonicalFloatStore(IntBitStore(value.toRawBits()))
    }

    override val isNegative: Boolean
        get() = bits[sizeBits - 1]

    override val biasedExponent: Int
        get() {
            val raw = (bits and exponentMask) shr (precision - 1)
            return raw.bits
        }

    override val rawSignificand: IntBitStore
        get() = (bits and significandMask)

    override val exponent: Int
        get() = biasedExponent - exponentBias

    override val significand: IntBitStore
        get() = rawSignificand.let {
            if (biasedExponent == 0) {
                it
            } else {
                it or hiddenBit
            }
        }

    override val isSubNormal: Boolean
        get() = biasedExponent == 0 && rawSignificand.bits != 0
    override val isZero: Boolean
        get() = biasedExponent == 0 && rawSignificand.bits == 0
    override val isFinite: Boolean
        get() = exponent <= maxExponent
    override val isNormal: Boolean
        get() = exponent >= minExponent
    override val isInfinity: Boolean
        get() = exponent == exponentBias + 1 && rawSignificand.bits == 0
    override val isNaN: Boolean
        get() = exponent == exponentBias + 1 && rawSignificand.bits != 0

    override val isMathematicalInteger: Boolean
        get() = isFinite && (isZero || precision - significand.trailingZeroes <= exponent)
    override val isPowerOfTwo: Boolean
        get() = if (isPositive && !isZero && isFinite) {
            significand.bits.isPowerOfTwo
        } else {
            false
        }

    override fun toFloat() = Float.fromBits(bits.bits)
}
