package com.kelvsyc.kotlin.core

/**
 * Implementation of [BitCollection] on [ByteArray], where bytes are arranged with the least significant byte first.
 */
object ByteArrayBitCollection : BitCollection<ByteArray> {
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
