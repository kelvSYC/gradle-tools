package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.BitCollection

object ByteBitCollection : BitCollection<Byte> {
    override fun fromBits(bits: IntRange): Byte {
        require(bits.start >= 0 && bits.endInclusive < Byte.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = (1 shl length) - 1
        return (mask shl bits.start).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: Byte): Sequence<Boolean> = sequence {
        var mask = 1
        for (i in 0 ..< Byte.SIZE_BITS) {
            yield(value.toInt() and mask != 0)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: Byte): ByteArray = ByteArray(Byte.SIZE_BYTES) { value }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: Byte): Set<Int> = buildSet {
        var mask = 1
        for (i in 0 ..< Byte.SIZE_BITS) {
            if (value.toInt() and mask != 0) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: Byte): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: Byte): Int = value.countTrailingZeroBits()
}

object UByteBitCollection : BitCollection<UByte> {
    override fun fromBits(bits: IntRange): UByte {
        require(bits.start >= 0 && bits.endInclusive < UByte.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = (1 shl length) - 1
        return (mask shl bits.start).toUByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: UByte): Sequence<Boolean> = sequence {
        var mask = 1
        for (i in 0 ..< UByte.SIZE_BITS) {
            yield(value.toInt() and mask != 0)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: UByte): ByteArray = ByteArray(UByte.SIZE_BYTES) { value.toByte() }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: UByte): Set<Int> = buildSet {
        var mask = 1
        for (i in 0 ..< UByte.SIZE_BITS) {
            if (value.toInt() and mask != 0) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: UByte): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: UByte): Int = value.countTrailingZeroBits()
}

object ShortBitCollection : BitCollection<Short> {
    override fun fromBits(bits: IntRange): Short {
        require(bits.start >= 0 && bits.endInclusive < Short.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = (1 shl length) - 1
        return (mask shl bits.start).toShort()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: Short): Sequence<Boolean> = sequence {
        var mask = 1
        for (i in 0 ..< Short.SIZE_BITS) {
            yield(value.toInt() and mask != 0)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: Short): ByteArray = ByteArray(Short.SIZE_BYTES) {
        (value.toInt() ushr (it * Byte.SIZE_BITS)).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: Short): Set<Int> = buildSet {
        var mask = 1
        for (i in 0 ..< Short.SIZE_BITS) {
            if (value.toInt() and mask != 0) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: Short): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: Short): Int = value.countTrailingZeroBits()
}

object UShortBitCollection : BitCollection<UShort> {
    override fun fromBits(bits: IntRange): UShort {
        require(bits.start >= 0 && bits.endInclusive < UShort.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = (1 shl length) - 1
        return (mask shl bits.start).toUShort()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: UShort): Sequence<Boolean> = sequence {
        var mask = 1
        for (i in 0 ..< UShort.SIZE_BITS) {
            yield(value.toInt() and mask != 0)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: UShort): ByteArray = ByteArray(UShort.SIZE_BYTES) {
        (value.toInt() ushr (it * Byte.SIZE_BITS)).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: UShort): Set<Int> = buildSet {
        var mask = 1
        for (i in 0 ..< UShort.SIZE_BITS) {
            if (value.toInt() and mask != 0) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: UShort): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: UShort): Int = value.countTrailingZeroBits()
}

object IntBitCollection : BitCollection<Int> {
    override fun fromBits(bits: IntRange): Int {
        require(bits.start >= 0 && bits.endInclusive < Int.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        // rhs of shl has a range that excludes sizeBits
        val mask = if (length == Int.SIZE_BITS) 0.inv() else (1 shl length) - 1
        return mask shl bits.start
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: Int): Sequence<Boolean> = sequence {
        var mask = 1
        for (i in 0 ..< Int.SIZE_BITS) {
            yield(value and mask != 0)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: Int): ByteArray = ByteArray(Int.SIZE_BYTES) {
        (value ushr (it * Byte.SIZE_BITS)).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: Int): Set<Int> = buildSet {
        var mask = 1
        for (i in 0 ..< Int.SIZE_BITS) {
            if (value and mask != 0) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: Int): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: Int): Int = value.countTrailingZeroBits()
}

object UIntBitCollection : BitCollection<UInt> {
    override fun fromBits(bits: IntRange): UInt {
        require(bits.start >= 0 && bits.endInclusive < UInt.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = if (length == UInt.SIZE_BITS) 0U.inv() else (1U shl length) - 1U
        return mask shl bits.start
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: UInt): Sequence<Boolean> = sequence {
        var mask = 1U
        for (i in 0 ..< UInt.SIZE_BITS) {
            yield(value and mask != 0U)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: UInt): ByteArray = ByteArray(UInt.SIZE_BYTES) {
        (value shr (it * Byte.SIZE_BITS)).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: UInt): Set<Int> = buildSet {
        var mask = 1U
        for (i in 0 ..< UInt.SIZE_BITS) {
            if (value and mask != 0U) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: UInt): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: UInt): Int = value.countTrailingZeroBits()
}

object LongBitCollection : BitCollection<Long> {
    override fun fromBits(bits: IntRange): Long {
        require(bits.start >= 0 && bits.endInclusive < Long.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = if (length == Long.SIZE_BITS) 0L.inv() else (1L shl length) - 1L
        return mask shl bits.start
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: Long): Sequence<Boolean> = sequence {
        var mask = 1L
        for (i in 0 ..< Long.SIZE_BITS) {
            yield(value and mask != 0L)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: Long): ByteArray = ByteArray(Long.SIZE_BYTES) {
        (value ushr (it * Byte.SIZE_BITS)).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: Long): Set<Int> = buildSet {
        var mask = 1L
        for (i in 0 ..< Long.SIZE_BITS) {
            if (value and mask != 0L) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: Long): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: Long): Int = value.countTrailingZeroBits()
}

object ULongBitCollection : BitCollection<ULong> {
    override fun fromBits(bits: IntRange): ULong {
        require(bits.start >= 0 && bits.endInclusive < ULong.SIZE_BITS) { "Bit collection contains values out of range" }

        val length = bits.endInclusive - bits.start + 1
        val mask = if (length == Long.SIZE_BITS) 0UL.inv() else (1UL shl length) - 1UL
        return mask shl bits.start
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun asBitSequence(value: ULong): Sequence<Boolean> = sequence {
        var mask = 1UL
        for (i in 0 ..< ULong.SIZE_BITS) {
            yield(value and mask != 0UL)
            mask = mask shl 1
        }
    }

    override fun asByteArray(value: ULong): ByteArray = ByteArray(Long.SIZE_BYTES) {
        (value shr (it * Byte.SIZE_BITS)).toByte()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun getSetBits(value: ULong): Set<Int> = buildSet {
        var mask = 1UL
        for (i in 0 ..< ULong.SIZE_BITS) {
            if (value and mask != 0UL) {
                add(i)
            }
            mask = mask shl 1
        }
    }

    override fun countLeadingZeroBits(value: ULong): Int = value.countLeadingZeroBits()
    override fun countTrailingZeroBits(value: ULong): Int = value.countTrailingZeroBits()
}
