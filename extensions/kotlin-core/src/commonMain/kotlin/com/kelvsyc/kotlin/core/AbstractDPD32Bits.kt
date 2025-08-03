package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.AbstractDecimal32Traits

/**
 * Partial implementation of the bit representation of a `decimal32` floating-point value, where the [significand] is
 * stored in densely packed decimal (DPD) encoding.
 *
 * BID encoding naturally assumes that the significant is a decimal number stored in a binary-coded decimal format,
 * represented here using [DenselyPackedDecimal].
 *
 * @param T the floating-point type
 */
abstract class AbstractDPD32Bits<T>(bits: Int, traits: AbstractDecimal32Traits<T>) : AbstractDecimal32Bits<T>(bits, traits) {
    override val biasedExponent: Int? by lazy {
        // In DPD, the combination field is subdivided into the top five bits and the bottom 6 bits
        // The top five bits contain 3 bits of the significand for low values and 1 bit for high values
        when (discriminator) {
            Discriminator.LOW -> combination.let { ((it and 0x0400) shr 3) or (it and 0x003F) }
            Discriminator.HIGH -> combination.let { ((it and 0x0180) shr 1) or (it and 0x003F) }
            else -> null
        }
    }

    private val lowSignificand by bitfield(this::bits, traits.continuationWidth + traits.exponentBits - 2, 3)
    private val highSignificand by bitfield(this::bits, traits.continuationWidth + traits.exponentBits - 2, 1)
    private val leadingDigit by lazy {
        when (discriminator) {
            Discriminator.LOW -> lowSignificand
            Discriminator.HIGH -> 8 + highSignificand
            else -> null
        }
    }
    override val significand: Int? by lazy {
        when (discriminator) {
            Discriminator.LOW, Discriminator.HIGH -> (leadingDigit!! shl traits.continuationWidth) or continuation
            else -> null
        }
    }
    @OptIn(ExperimentalStdlibApi::class)
    override val significandAsNumber: Int? by lazy {
        // Probably easiest to dump every 10-bit block of the continuation into a string, then convert to Int
        leadingDigit?.let {
            val stringForm = buildString {
                append(it)
                val blockCount = (traits.precision - 1) / 3
                for (i in blockCount - 1 downTo 0) {
                    val block by bitfield(this@AbstractDPD32Bits::bits, i * 10, 10, TypeTraits.Int, Int::toShort)
                    val dpd = DenselyPackedDecimal(block)
                    append(dpd.asString)
                }
            }
            // FIXME JVM's toInt() will ignore leading zeroes, but other platforms might not
            stringForm.toInt()
        }
    }
    override val isZero: Boolean by lazy { significand == 0 }

    override val isNormal: Boolean by lazy { leadingDigit != 0 }
    override val isDenormal: Boolean by lazy { leadingDigit == 0 }
    override val isSubnormal: Boolean by lazy { isDenormal && biasedExponent == 0 }
}
