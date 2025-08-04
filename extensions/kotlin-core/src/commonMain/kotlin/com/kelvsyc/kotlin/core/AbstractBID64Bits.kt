package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.Decimal64Traits

/**
 * Partial implementation of the bit representation of a `decimal64` floating-point value, where the [significand] is
 * stored in binary integer decimal (BID) encoding.
 *
 * BID encoding naturally assumes that the significant is a decimal number stored as a binary number - as such,
 * [significand] and [significandAsNumber] are identical and return the same values. Note that no attempt is made to
 * verify that the value returned is within the allowable range of values.
 *
 * @param T the floating-point type
 */
abstract class AbstractBID64Bits<T>(bits: Long, traits: Decimal64Traits<T>) : AbstractDecimal64Bits<T>(bits, traits) {
    private val lowExponent by bitfield(
        this::bits, traits.sizeBits - traits.exponentBits - 1, traits.exponentBits, TypeTraits.Long, Long::toInt)
    private val highExponent by bitfield(
        this::bits, traits.sizeBits - traits.exponentBits - 3, traits.exponentBits, TypeTraits.Long, Long::toInt)

    override val biasedExponent: Int? by lazy {
        when (discriminator) {
            Discriminator.LOW -> lowExponent
            Discriminator.HIGH -> highExponent
            else -> null
        }
    }

    private val lowSignificand by bitfield(this::bits, 0, traits.continuationWidth + 3)
    private val highSignificand by bitfield(this::bits, 0, traits.continuationWidth + 1)
    override val significand: Long? by lazy {
        when (discriminator) {
            Discriminator.LOW -> lowSignificand
            Discriminator.HIGH -> highSignificand or (1L shl (traits.significandBits - 1))
            else -> null
        }
    }
    override val significandAsNumber: Long? by this::significand
    override val isZero: Boolean by lazy { significand == 0L }

    override val isNormal: Boolean by lazy { significand?.let { it >= 1000000000000000L } == true }
    override val isDenormal: Boolean by lazy { significand?.let { it < 1000000000000000L } == true }
    override val isSubnormal: Boolean by lazy { isDenormal && biasedExponent == 0 }
}
