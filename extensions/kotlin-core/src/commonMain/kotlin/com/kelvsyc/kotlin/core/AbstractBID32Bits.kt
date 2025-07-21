package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.Decimal32Traits

/**
 * Partial implementation of the bit representation of a `decimal32` floating-point value, where the [significand] is
 * stored in binary integer decimal (BID) encoding.
 *
 * BID encoding naturally assumes that the significant is a decimal number stored as a binary number - as such,
 * [significand] and [significandAsNumber] are identical and return the same values. Note that no attempt is made to
 * verify that the value returned is within the allowable range of values.
 *
 * @param T the floating-point type
 */
abstract class AbstractBID32Bits<T>(bits: Int, traits: Decimal32Traits<T>) : AbstractDecimal32Bits<T>(bits, traits) {
    private val lowExponent by bitfield(this::bits, traits.sizeBits - traits.exponentBits - 1, traits.exponentBits)
    private val highExponent by bitfield(this::bits, traits.sizeBits - traits.exponentBits - 3, traits.exponentBits)

    override val biasedExponent: Int? by lazy {
        when (discriminator) {
            Discriminator.LOW -> lowExponent
            Discriminator.HIGH -> highExponent
            else -> null
        }
    }

    private val lowSignificand by bitfield(this::bits, 0, traits.continuationWidth + 3)
    private val highSignificand by bitfield(this::bits, 0, traits.continuationWidth + 1)
    override val significand: Int? by lazy {
        when (discriminator) {
            Discriminator.LOW -> lowSignificand
            Discriminator.HIGH -> highSignificand or (1 shl (traits.significandBits - 1))
            else -> null
        }
    }
    override val significandAsNumber: Int? by this::significand
    override val isZero: Boolean by lazy { significand == 0 }

    override val isNormal: Boolean by lazy { significand?.let { it >= 1000000 } == true }
    override val isDenormal: Boolean by lazy { significand?.let { it < 1000000 } == true }
    override val isSubnormal: Boolean by lazy { isDenormal && biasedExponent == 0 }
}
