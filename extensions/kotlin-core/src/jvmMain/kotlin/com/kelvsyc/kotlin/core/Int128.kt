package com.kelvsyc.kotlin.core

import org.apache.commons.io.EndianUtils
import java.math.BigInteger

/**
 * Representation of a 128-bit integer, or "long long", represented by two [Long] values.
 *
 * Operations are performed by converting the values to a [BigInteger] and operating on those values.
 */
class Int128(val high: Long, val low: Long) : Comparable<Int128> {
    companion object {
        /**
         * The number of bits used to represent an [Int128] in a binary form.
         */
        const val SIZE_BITS = 2 * Long.SIZE_BITS

        /**
         * The number of bytes used to represent an [Int128] in a binary form.
         */
        const val SIZE_BYTES = 2 * Long.SIZE_BYTES

        /**
         * [Converter] implementation converting [Int128] to [BigInteger] for use with operations.
         */
        val converter = Converter.of(Int128::toBigInteger, Companion::of)

        /**
         * [Comparator] implementation, comparing two [Int128] instances by performing the comparison as [BigInteger]
         * values.
         */
        val comparator = Comparator.comparing(converter)

        fun of(value: Long) = Int128(0, value)
        @OptIn(ExperimentalStdlibApi::class)
        fun of(value: BigInteger): Int128 {
            val raw = value.toByteArray()
            // raw might not be exactly 8 bits
            val formatted = ByteArray(SIZE_BYTES) {
                if (it < raw.size) {
                    // Switch it up from big to little endian
                    raw[raw.size - 1 - it]
                } else {
                    // We need to "sign extend"
                    if (value.signum() < 0) 0xFF.toByte() else 0
                }
            }
            val values = (0 ..< SIZE_BYTES / Long.SIZE_BYTES).map {
                EndianUtils.readSwappedLong(formatted, it * Long.SIZE_BYTES)
            }
            return Int128(values[1], values[0])
        }

        private val wrappedUnaryMinus = converter.reverse.wrap(BigInteger::negate)
        private val wrappedPlus = converter.reverse.wrap(BigInteger::add)
        private val wrappedMinus = converter.reverse.wrap(BigInteger::subtract)
        private val wrappedTimes = converter.reverse.wrap(BigInteger::multiply)
        private val wrappedDiv = converter.reverse.wrap(BigInteger::divide)
        private val wrappedRem = converter.reverse.wrap(BigInteger::remainder)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun toBigInteger(): BigInteger {
        val bytes = ByteArray(SIZE_BYTES)
        EndianUtils.writeSwappedLong(bytes, 0, low)
        EndianUtils.writeSwappedLong(bytes, Long.SIZE_BYTES, high)
        bytes.reverse() // Switch from little-endian to big-endian
        return BigInteger(bytes)
    }

    operator fun unaryPlus(): Int128 = this
    operator fun unaryMinus(): Int128 = wrappedUnaryMinus(this)
    operator fun plus(rhs: Int128): Int128 = wrappedPlus(this, rhs)
    operator fun minus(rhs: Int128): Int128 = wrappedMinus(this, rhs)
    operator fun times(rhs: Int128): Int128 = wrappedTimes(this, rhs)
    operator fun div(rhs: Int128): Int128 = wrappedDiv(this, rhs)
    operator fun rem(rhs: Int128): Int128 = wrappedRem(this, rhs)

    override fun compareTo(other: Int128): Int = comparator.compare(this, other)
}
