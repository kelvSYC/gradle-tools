package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.AbstractDecimal64Traits

/**
 * Partial implementation of the bit representation of a `decimal64` floating-point value.
 *
 * @param T the floating-point type
 */
abstract class AbstractDecimal64Bits<T>(
    protected val bits: Long,
    protected val traits: AbstractDecimal64Traits<T>
) : DecimalFloatingPointBits<T, Long> {
    protected enum class Discriminator {
        LOW, HIGH, INFINITE, NAN
    }

    override val signBit: Boolean by flag(this::bits, traits.sizeBits - 1)
    override val combination: Int by bitfield(
        this::bits, traits.continuationWidth, traits.combinationWidth, TypeTraits.Long, Long::toInt)
    override val continuation: Long by bitfield(this::bits, 0, traits.continuationWidth)

    override val exponent: Int? by lazy { biasedExponent?.let {it - traits.exponentBias} }
    override val integralExponent: Int? by lazy { biasedExponent?.let {it - traits.integralExponentBias} }
    override val payload: Long? by lazy { payloadInternal.takeIf {isNaN} }

    private val topBits by bitfield(this::bits, traits.sizeBits - 6, 5, TypeTraits.Long, Long::toInt)
    protected val discriminator by lazy {
        topBits.let {
            if (it and 0x18 != 0x18) Discriminator.LOW
            else if (it and 0x1E != 0x1E) Discriminator.HIGH
            else if (it == 0x1E) Discriminator.INFINITE
            else Discriminator.NAN
        }
    }

    private val signalling by flag(this::bits, traits.sizeBits - 7)
    private val payloadInternal by bitfield(this::bits, 0, traits.sizeBits - 7)
    override val isInfinite: Boolean by lazy { discriminator == Discriminator.INFINITE }
    override val isNaN: Boolean by lazy { discriminator == Discriminator.NAN }
    override val isQuietNaN: Boolean by lazy { isNaN && !signalling }
    override val isSignallingNaN: Boolean by lazy { isNaN && signalling }
}
