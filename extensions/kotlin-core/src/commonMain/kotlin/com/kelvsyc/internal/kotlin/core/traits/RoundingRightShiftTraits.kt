package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.RoundingRightShift

object ByteRoundingRightShift : RoundingRightShift<Byte> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > Byte.SIZE_BITS) return 0
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == Byte.SIZE_BITS) {
            // You are shifting the whole value off
            0.toByte() to value
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = (value.toUByte().toInt() ushr bitCount).toByte()
            val remainder = (value.toUByte().toInt() shl (Byte.SIZE_BITS - bitCount)).toByte()
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Byte.MIN_VALUE || result.toInt() and 1 == 1)) {
            // Round up
            (result + 1).toByte()
        } else {
            result
        }
    }
}

object UByteRoundingRightShift : RoundingRightShift<UByte> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > UByte.SIZE_BITS) return 0U
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == UByte.SIZE_BITS) {
            // You are shifting the whole value off
            0.toUByte() to value.toByte()
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = (value.toInt() ushr bitCount).toUByte()
            val remainder = (value.toInt() shl (Byte.SIZE_BITS - bitCount)).toByte()
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Byte.MIN_VALUE || result.toUInt() and 1U == 1U)) {
            // Round up
            (result + 1U).toUByte()
        } else {
            result
        }
    }
}

object ShortRoundingRightShift : RoundingRightShift<Short> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > Short.SIZE_BITS) return 0
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == Short.SIZE_BITS) {
            // You are shifting the whole value off
            0.toShort() to value
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = (value.toUShort().toInt() ushr bitCount).toShort()
            val remainder = (value.toUShort().toInt() shl (Short.SIZE_BITS - bitCount)).toShort()
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Short.MIN_VALUE || result.toInt() and 1 == 1)) {
            // Round up
            (result + 1).toShort()
        } else {
            result
        }
    }
}

object UShortRoundingRightShift : RoundingRightShift<UShort> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > UShort.SIZE_BITS) return 0U
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == UShort.SIZE_BITS) {
            // You are shifting the whole value off
            0.toUShort() to value.toShort()
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = (value.toInt() ushr bitCount).toUShort()
            val remainder = (value.toInt() shl (UShort.SIZE_BITS - bitCount)).toShort()
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Short.MIN_VALUE || result.toUInt() and 1U == 1U)) {
            // Round up
            (result + 1U).toUShort()
        } else {
            result
        }
    }
}

object IntRoundingRightShift : RoundingRightShift<Int> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > Int.SIZE_BITS) return 0
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == Int.SIZE_BITS) {
            // You are shifting the whole value off
            0 to value
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = value ushr bitCount
            val remainder = value shl (Int.SIZE_BITS - bitCount)
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Int.MIN_VALUE || result and 1 == 1)) {
            // Round up
            result + 1
        } else {
            result
        }
    }
}

object UIntRoundingRightShift : RoundingRightShift<UInt> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > UInt.SIZE_BITS) return 0U
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == UInt.SIZE_BITS) {
            // You are shifting the whole value off
            0U to value.toInt()
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = value shr bitCount
            val remainder = (value shl (UInt.SIZE_BITS - bitCount)).toInt()
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Int.MIN_VALUE || result and 1U == 1U)) {
            // Round up
            result + 1U
        } else {
            result
        }
    }
}

object LongRoundingRightShift : RoundingRightShift<Long> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > Long.SIZE_BITS) return 0
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == Long.SIZE_BITS) {
            // You are shifting the whole value off
            0L to value
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = value ushr bitCount
            val remainder = value shl (Long.SIZE_BITS - bitCount)
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Long.MIN_VALUE || result and 1L == 1L)) {
            // Round up
            result + 1L
        } else {
            result
        }
    }
}

object ULongRoundingRightShift : RoundingRightShift<ULong> {
    @Suppress("detekt:ReturnCount")
    override fun roundingRightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative rounding right shifts are unsupported." }
        if (bitCount > ULong.SIZE_BITS) return 0U
        if (bitCount == 0) return value

        val (result, remainder) = if (bitCount == ULong.SIZE_BITS) {
            // You are shifting the whole value off
            0UL to value.toLong()
        } else {
            // Remainder consists of the bits shifted-off, left aligned to make it look like a fractional part
            val result = value shr bitCount
            val remainder = (value shl (ULong.SIZE_BITS - bitCount)).toLong()
            result to remainder
        }

        // If the result is odd:
        //     If the MSB of the remainder is a 1 (ie. remainder < 0), we round up
        //     Otherwise we round down
        // If the result is even:
        //     If the MSB of the remainder is a 1
        //         If the MSB is the only set bit in the remainder (ie. remainder == MIN_VALUE), we round down
        //             (this is the "half-even tiebreaker")
        //         Otherwise, we round up
        //     Otherwise we round down
        return if ((remainder < 0) && (remainder != Long.MIN_VALUE || result and 1UL == 1UL)) {
            // Round up
            result + 1UL
        } else {
            result
        }
    }
}
