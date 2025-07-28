package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.RightShift

interface ByteRightShift : RightShift<Byte> {
    override fun rightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Byte.SIZE_BITS) 0.toByte()
        else (value.toUByte().toInt() ushr bitCount).toByte()
    }
}

interface UByteRightShift : RightShift<UByte> {
    override fun rightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Byte.SIZE_BITS) 0.toUByte()
        else (value.toInt() shr bitCount).toUByte()
    }
}

interface ShortRightShift : RightShift<Short> {
    override fun rightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Short.SIZE_BITS) 0.toShort()
        else (value.toUShort().toInt() ushr bitCount).toShort()
    }
}

interface UShortRightShift : RightShift<UShort> {
    override fun rightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UShort.SIZE_BITS) 0.toUShort()
        else (value.toInt() shr bitCount).toUShort()
    }
}

interface IntRightShift : RightShift<Int> {
    override fun rightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Int.SIZE_BITS) 0
        else value ushr bitCount
    }
}

interface UIntRightShift : RightShift<UInt> {
    override fun rightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UInt.SIZE_BITS) 0U
        else value shr bitCount
    }
}

interface LongRightShift : RightShift<Long> {
    override fun rightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Long.SIZE_BITS) 0L
        else value ushr bitCount
    }
}

interface ULongRightShift : RightShift<ULong> {
    override fun rightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= ULong.SIZE_BITS) 0UL
        else value shr bitCount
    }
}

interface ByteArrayRightShift : RightShift<ByteArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: ByteArray, bitCount: Int): ByteArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Byte.SIZE_BITS) return ByteArray(value.size)

        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (Byte.SIZE_BITS - shiftMod)

        return ByteArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toUByte().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                (src or dst).toByte()
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UByteArrayRightShift : RightShift<UByteArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: UByteArray, bitCount: Int): UByteArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UByte.SIZE_BITS) return UByteArray(value.size)

        val offsetBytes = bitCount / UByte.SIZE_BITS
        val shiftMod = bitCount % UByte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (UByte.SIZE_BITS - shiftMod)

        return UByteArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (UByte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                (src or dst).toUByte()
            }
        }
    }
}

interface ShortArrayRightShift : RightShift<ShortArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: ShortArray, bitCount: Int): ShortArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Short.SIZE_BITS) return ShortArray(value.size)

        val offsetBytes = bitCount / Short.SIZE_BITS
        val shiftMod = bitCount % Short.SIZE_BITS
        val carryMask = UShort.MAX_VALUE.toInt() shl (Short.SIZE_BITS - shiftMod)

        return ShortArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toUShort().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (Short.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                (src or dst).toShort()
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UShortArrayRightShift : RightShift<UShortArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: UShortArray, bitCount: Int): UShortArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UShort.SIZE_BITS) return UShortArray(value.size)

        val offsetBytes = bitCount / UShort.SIZE_BITS
        val shiftMod = bitCount % UShort.SIZE_BITS
        val carryMask = UShort.MAX_VALUE.toInt() shl (UShort.SIZE_BITS - shiftMod)

        return UShortArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (UShort.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                (src or dst).toUShort()
            }
        }
    }
}

interface IntArrayRightShift : RightShift<IntArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: IntArray, bitCount: Int): IntArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Int.SIZE_BITS) return IntArray(value.size)

        val offsetBytes = bitCount / Int.SIZE_BITS
        val shiftMod = bitCount % Int.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0 else UInt.MAX_VALUE.toInt() shl (Int.SIZE_BITS - shiftMod)

        return IntArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (Int.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                src or dst
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UIntArrayRightShift : RightShift<UIntArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: UIntArray, bitCount: Int): UIntArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UInt.SIZE_BITS) return UIntArray(value.size)

        val offsetBytes = bitCount / UInt.SIZE_BITS
        val shiftMod = bitCount % UInt.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0U else UInt.MAX_VALUE shl (UInt.SIZE_BITS - shiftMod)

        return UIntArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] shr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (UInt.SIZE_BITS - shiftMod)) and carryMask
                } else 0U
                src or dst
            }
        }
    }
}

interface LongArrayRightShift : RightShift<LongArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: LongArray, bitCount: Int): LongArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Long.SIZE_BITS) return LongArray(value.size)

        val offsetBytes = bitCount / Long.SIZE_BITS
        val shiftMod = bitCount % Long.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0 else ULong.MAX_VALUE.toLong() shl (Long.SIZE_BITS - shiftMod)

        return LongArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0L
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (Long.SIZE_BITS - shiftMod)) and carryMask
                } else 0L
                src or dst
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface ULongArrayRightShift : RightShift<ULongArray> {
    @Suppress("detekt:ReturnCount")
    override fun rightShift(value: ULongArray, bitCount: Int): ULongArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * ULong.SIZE_BITS) return ULongArray(value.size)

        val offsetBytes = bitCount / ULong.SIZE_BITS
        val shiftMod = bitCount % ULong.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0U else ULong.MAX_VALUE shl (ULong.SIZE_BITS - shiftMod)

        return ULongArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                0UL
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] shr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (ULong.SIZE_BITS - shiftMod)) and carryMask
                } else 0UL
                src or dst
            }
        }
    }
}
