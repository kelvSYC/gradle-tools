package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.LeftShift

interface ByteLeftShift : LeftShift<Byte> {
    override fun leftShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Byte.SIZE_BITS) 0.toByte()
        else (value.toInt() shl bitCount).toByte()
    }
}

interface UByteLeftShift : LeftShift<UByte> {
    override fun leftShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UByte.SIZE_BITS) 0.toUByte()
        else (value.toInt() shl bitCount).toUByte()
    }
}

interface ShortLeftShift : LeftShift<Short> {
    override fun leftShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Short.SIZE_BITS) 0.toShort()
        else (value.toInt() shl bitCount).toShort()
    }
}

interface UShortLeftShift : LeftShift<UShort> {
    override fun leftShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UShort.SIZE_BITS) 0.toUShort()
        else (value.toInt() shl bitCount).toUShort()
    }
}

interface IntLeftShift : LeftShift<Int> {
    override fun leftShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Int.SIZE_BITS) 0
        else value shl bitCount
    }
}

interface UIntLeftShift : LeftShift<UInt> {
    override fun leftShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UInt.SIZE_BITS) 0U
        else value shl bitCount
    }
}

interface LongLeftShift : LeftShift<Long> {
    override fun leftShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Long.SIZE_BITS) 0L
        else value shl bitCount
    }
}

interface ULongLeftShift : LeftShift<ULong> {
    override fun leftShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= ULong.SIZE_BITS) 0UL
        else value shl bitCount
    }
}

interface ByteArrayLeftShift : LeftShift<ByteArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: ByteArray, bitCount: Int): ByteArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Byte.SIZE_BITS) return ByteArray(value.size)

        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val result = ByteArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = (value[sourceIndex].toInt() shl shiftMod)
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1].toInt() ushr (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = (src or dst).toByte()
            }
        }
        return result
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UByteArrayLeftShift : LeftShift<UByteArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: UByteArray, bitCount: Int): UByteArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UByte.SIZE_BITS) return UByteArray(value.size)

        val offsetBytes = bitCount / UByte.SIZE_BITS
        val shiftMod = bitCount % UByte.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val result = UByteArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0U
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = (value[sourceIndex].toInt() shl shiftMod)
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1].toInt() ushr (UByte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = (src or dst).toUByte()
            }
        }
        return result
    }
}

interface ShortArrayLeftShift : LeftShift<ShortArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: ShortArray, bitCount: Int): ShortArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Short.SIZE_BITS) return ShortArray(value.size)

        val offsetBytes = bitCount / Short.SIZE_BITS
        val shiftMod = bitCount % Short.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val result = ShortArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = (value[sourceIndex].toInt() shl shiftMod)
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1].toInt() ushr (Short.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = (src or dst).toShort()
            }
        }
        return result
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UShortArrayLeftShift : LeftShift<UShortArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: UShortArray, bitCount: Int): UShortArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UShort.SIZE_BITS) return UShortArray(value.size)

        val offsetBytes = bitCount / UShort.SIZE_BITS
        val shiftMod = bitCount % UShort.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val result = UShortArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0U
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = (value[sourceIndex].toInt() shl shiftMod)
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1].toInt() ushr (UShort.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = (src or dst).toUShort()
            }
        }
        return result
    }
}

interface IntArrayLeftShift : LeftShift<IntArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: IntArray, bitCount: Int): IntArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Int.SIZE_BITS) return IntArray(value.size)

        val offsetBytes = bitCount / Int.SIZE_BITS
        val shiftMod = bitCount % Int.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val result = IntArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = value[sourceIndex] shl shiftMod
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1] ushr (Int.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = src or dst
            }
        }
        return result
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UIntArrayLeftShift : LeftShift<UIntArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: UIntArray, bitCount: Int): UIntArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UInt.SIZE_BITS) return UIntArray(value.size)

        val offsetBytes = bitCount / UInt.SIZE_BITS
        val shiftMod = bitCount % UInt.SIZE_BITS
        val carryMask = (1U shl shiftMod) - 1U

        val result = UIntArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0U
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = value[sourceIndex] shl shiftMod
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1] shr (UInt.SIZE_BITS - shiftMod)) and carryMask
                } else 0U
                result[i] = src or dst
            }
        }
        return result
    }
}

interface LongArrayLeftShift : LeftShift<LongArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: LongArray, bitCount: Int): LongArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Long.SIZE_BITS) return LongArray(value.size)

        val offsetBytes = bitCount / Long.SIZE_BITS
        val shiftMod = bitCount % Long.SIZE_BITS
        val carryMask = (1L shl shiftMod) - 1L

        val result = LongArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0L
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = value[sourceIndex] shl shiftMod
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1] ushr (Long.SIZE_BITS - shiftMod)) and carryMask
                } else 0L
                result[i] = src or dst
            }
        }
        return result
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface ULongArrayLeftShift : LeftShift<ULongArray> {
    @Suppress("detekt:ReturnCount")
    override fun leftShift(value: ULongArray, bitCount: Int): ULongArray {
        require(bitCount >= 0) { "Negative left shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * ULong.SIZE_BITS) return ULongArray(value.size)

        val offsetBytes = bitCount / ULong.SIZE_BITS
        val shiftMod = bitCount % ULong.SIZE_BITS
        val carryMask = (1UL shl shiftMod) - 1UL

        val result = ULongArray(value.size)
        for (i in result.size - 1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0UL
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = value[sourceIndex] shl shiftMod
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1] shr (ULong.SIZE_BITS - shiftMod)) and carryMask
                } else 0UL
                result[i] = src or dst
            }
        }
        return result
    }
}
