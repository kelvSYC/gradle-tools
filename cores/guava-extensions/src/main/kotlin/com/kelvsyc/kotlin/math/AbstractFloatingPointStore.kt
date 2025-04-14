package com.kelvsyc.kotlin.math

/**
 * Base class for custom implementations for [FloatingPointStore].
 *
 * Note that this is not a value class wrapper.
 */
abstract class AbstractFloatingPointStore<F : AbstractFloatingPointStore<F, S, R>, S : BitStore<S, R>, R> : FloatingPointStore<S, R> {
    protected abstract val traits: FloatingPointStore.AbstractCompanion<F, S, R>

    override val isNegative by lazy {
        bits[traits.sizeBits - 1]
    }

    override val biasedExponent by lazy {
        val raw = (bits and traits.exponentMask) shr (traits.precision - 1)
        traits.rawToInt(raw.bits)
    }

    override val rawSignificand: S by lazy {
        bits and traits.significandMask
    }

    override val exponent by lazy {
        biasedExponent - traits.exponentBias
    }

    override val significand: S by lazy {
        rawSignificand.let {
            if (biasedExponent == 0) {
                it
            } else {
                it or traits.hiddenBit
            }
        }
    }

    override val isSubNormal by lazy {
        biasedExponent == 0 && !traits.rawIsZero(rawSignificand.bits)
    }
    override val isZero: Boolean by lazy {
        biasedExponent == 0 && traits.rawIsZero(rawSignificand.bits)
    }
    override val isFinite by lazy {
        exponent <= traits.maxExponent
    }
    override val isNormal by lazy {
        exponent >= traits.minExponent
    }
    override val isInfinity by lazy {
        exponent == traits.exponentBias + 1 && traits.rawIsZero(rawSignificand.bits)
    }
    override val isNaN by lazy {
        exponent == traits.exponentBias + 1 && !traits.rawIsZero(rawSignificand.bits)
    }

    override val isMathematicalInteger by lazy {
        isFinite && (isZero || traits.precision - significand.trailingZeroes <= exponent)
    }
    override val isPowerOfTwo by lazy {
        if (isPositive && !isZero && isFinite) {
            // The significand must be a power of two
            // This is inefficient, but we can't do better without knowing more about R
            significand.asSet().size == 1
        } else {
            false
        }
    }
}
