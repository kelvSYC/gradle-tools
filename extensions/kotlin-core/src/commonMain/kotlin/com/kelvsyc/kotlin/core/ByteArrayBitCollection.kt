package com.kelvsyc.kotlin.core

/**
 * Implementation of [BitCollection] on [ByteArray], where bytes are arranged with the least significant byte first.
 */
object ByteArrayBitCollection : BitCollection<ByteArray> {
    override fun fromBits(bits: IntRange): ByteArray {
        val size = bits.endInclusive.ceilDiv(Byte.SIZE_BITS)

        val startIndex = bits.start / Byte.SIZE_BITS
        val endIndex = bits.endInclusive / Byte.SIZE_BITS

        val result = ByteArray(size)
        if (startIndex == endIndex) {
            // The start and end index are in the same byte, so only that byte needs to be set
            val length = bits.endInclusive - bits.start
            val value = ((1 shl length) - 1).toByte()
            result[startIndex] = value
        } else {
            // The start and end index are in different bytes and thus require special values
            // All bits in between the two indices should be set to 0xFF
            val startBit = bits.start.rem(Byte.SIZE_BITS)
            val endBit = bits.endInclusive.rem(Byte.SIZE_BITS)

            @Suppress("detekt:MagicNumber")
            val startValue = (0xFF shl startBit).toByte()
            val endValue = ((1 shl endBit) - 1).toByte()

            result[startIndex] = startValue
            for (i in startIndex + 1 .. endIndex - 1) { result[i] = 0xFF.toByte() }
            result[endIndex] = endValue
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: ByteArray): Sequence<Boolean> = sequence {
        value.forEach {
            var mask = 1
            for (i in 0 ..< Byte.SIZE_BITS) {
                yield(it.toInt() and mask != 0)
                mask = mask shl 1
            }
        }
    }

    override fun asByteArray(value: ByteArray): ByteArray = value

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: ByteArray): Set<Int>  = buildSet {
        value.forEachIndexed { index, b ->
            var mask = 1
            for (i in 0 ..< Byte.SIZE_BITS) {
                if (b.toInt() and mask != 0) {
                    add((index shl 3) or i)
                }
                mask = mask shl 1
            }
        }
    }

    override fun isZero(value: ByteArray): Boolean = value.all { it.toInt() == 0 }

    override fun countLeadingZeroBits(value: ByteArray): Int {
        val idx = value.indexOfLast { it.countLeadingZeroBits() != Byte.SIZE_BITS }
        return if (idx == -1) {
            // The byte array is all zeroes
            value.size * Byte.SIZE_BITS
        } else {
            (value.size - 1 - idx) * Byte.SIZE_BITS + value[idx].countLeadingZeroBits()
        }
    }

    override fun countTrailingZeroBits(value: ByteArray): Int {
        val idx = value.indexOfFirst { it.countTrailingZeroBits() != Byte.SIZE_BITS }
        return if (idx == -1) {
            // The byte array is all zeroes
            value.size * Byte.SIZE_BITS
        } else {
            idx * Byte.SIZE_BITS + value[idx].countTrailingZeroBits()
        }
    }
}
