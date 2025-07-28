package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.ArithmeticRightShift

interface ByteArithmeticRightShift : ArithmeticRightShift<Byte> {
    override fun arithmeticRightShift(value: Byte, bitCount: Int): Byte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Byte.SIZE_BITS) if (value < 0) UByte.MAX_VALUE.toByte() else 0.toByte()
        else (value.toInt() shr bitCount).toByte()
    }
}

interface UByteArithmeticRightShift : ArithmeticRightShift<UByte> {
    override fun arithmeticRightShift(value: UByte, bitCount: Int): UByte {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UByte.SIZE_BITS) if (value.toByte() < 0) UByte.MAX_VALUE else 0.toUByte()
        else (value.toByte().toInt() shr bitCount).toUByte()
    }
}

interface ShortArithmeticRightShift : ArithmeticRightShift<Short> {
    override fun arithmeticRightShift(value: Short, bitCount: Int): Short {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Short.SIZE_BITS) if (value < 0) UShort.MAX_VALUE.toShort() else 0.toShort()
        else (value.toInt() shr bitCount).toShort()
    }
}

interface UShortArithmeticRightShift : ArithmeticRightShift<UShort> {
    override fun arithmeticRightShift(value: UShort, bitCount: Int): UShort {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UShort.SIZE_BITS) if (value.toShort() < 0) UShort.MAX_VALUE else 0.toUShort()
        else (value.toShort().toInt() shr bitCount).toUShort()
    }
}

interface IntArithmeticRightShift : ArithmeticRightShift<Int> {
    override fun arithmeticRightShift(value: Int, bitCount: Int): Int {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Int.SIZE_BITS) if (value < 0) UInt.MAX_VALUE.toInt() else 0
        else value shr bitCount
    }
}

interface UIntArithmeticRightShift : ArithmeticRightShift<UInt> {
    override fun arithmeticRightShift(value: UInt, bitCount: Int): UInt {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= UInt.SIZE_BITS) if (value.toInt() < 0) UInt.MAX_VALUE else 0U
        else (value.toInt() shr bitCount).toUInt()
    }
}

interface LongArithmeticRightShift : ArithmeticRightShift<Long> {
    override fun arithmeticRightShift(value: Long, bitCount: Int): Long {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= Long.SIZE_BITS) if (value < 0L) ULong.MAX_VALUE.toLong() else 0L
        else value shr bitCount
    }
}

interface ULongArithmeticRightShift : ArithmeticRightShift<ULong> {
    override fun arithmeticRightShift(value: ULong, bitCount: Int): ULong {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        return if (bitCount == 0) value
        else if (bitCount >= ULong.SIZE_BITS) if (value.toLong() < 0L) ULong.MAX_VALUE else 0UL
        else (value.toLong() shr bitCount).toULong()
    }
}

interface ByteArrayArithmeticRightShift : ArithmeticRightShift<ByteArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: ByteArray, bitCount: Int): ByteArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Byte.SIZE_BITS) return ByteArray(value.size) {
            // We shifted the whole value off
            if (value.last() < 0) UByte.MAX_VALUE.toByte() else 0
        }

        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (Byte.SIZE_BITS - shiftMod)
        val sign = value.last() < 0

        return ByteArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) UByte.MAX_VALUE.toByte() else 0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (8 - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toUByte().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UByte.MAX_VALUE.toInt() and carryMask else 0
                }
                (src or dst).toByte()
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UByteArrayArithmeticRightShift : ArithmeticRightShift<UByteArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: UByteArray, bitCount: Int): UByteArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UByte.SIZE_BITS) return UByteArray(value.size) {
            // We shifted the whole value off
            if (value.last().toByte() < 0) UByte.MAX_VALUE else 0U
        }

        val offsetBytes = bitCount / UByte.SIZE_BITS
        val shiftMod = bitCount % UByte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (UByte.SIZE_BITS - shiftMod)
        val sign = value.last().toByte() < 0

        return UByteArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) UByte.MAX_VALUE else 0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (UByte.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UByte.MAX_VALUE.toInt() and carryMask else 0
                }
                (src or dst).toUByte()
            }
        }
    }
}

interface ShortArrayArithmeticRightShift : ArithmeticRightShift<ShortArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: ShortArray, bitCount: Int): ShortArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Short.SIZE_BITS) return ShortArray(value.size) {
            // We shifted the whole value off
            if (value.last() < 0) UShort.MAX_VALUE.toShort() else 0
        }

        val offsetBytes = bitCount / Short.SIZE_BITS
        val shiftMod = bitCount % Short.SIZE_BITS
        val carryMask = UShort.MAX_VALUE.toInt() shl (Short.SIZE_BITS - shiftMod)
        val sign = value.last() < 0

        return ShortArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) UShort.MAX_VALUE.toShort() else 0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toUShort().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (Short.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UShort.MAX_VALUE.toInt() and carryMask else 0
                }
                (src or dst).toShort()
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UShortArrayArithmeticRightShift : ArithmeticRightShift<UShortArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: UShortArray, bitCount: Int): UShortArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UShort.SIZE_BITS) return UShortArray(value.size) {
            // We shifted the whole value off
            if (value.last().toShort() < 0) UShort.MAX_VALUE else 0U
        }

        val offsetBytes = bitCount / UShort.SIZE_BITS
        val shiftMod = bitCount % UShort.SIZE_BITS
        val carryMask = UShort.MAX_VALUE.toInt() shl (UShort.SIZE_BITS - shiftMod)
        val sign = value.last().toShort() < 0

        return UShortArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) UShort.MAX_VALUE else 0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1].toInt() shl (UShort.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UShort.MAX_VALUE.toInt() and carryMask else 0
                }
                (src or dst).toUShort()
            }
        }
    }
}

interface IntArrayArithmeticRightShift : ArithmeticRightShift<IntArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: IntArray, bitCount: Int): IntArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Int.SIZE_BITS) return IntArray(value.size) {
            // We shifted the whole value off
            if (value.last() < 0) UInt.MAX_VALUE.toInt() else 0
        }

        val offsetBytes = bitCount / Int.SIZE_BITS
        val shiftMod = bitCount % Int.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0 else UInt.MAX_VALUE.toInt() shl (Int.SIZE_BITS - shiftMod)
        val sign = value.last() < 0

        return IntArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) UInt.MAX_VALUE.toInt() else 0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (Int.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UInt.MAX_VALUE.toInt() and carryMask else 0
                }
                src or dst
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UIntArrayArithmeticRightShift : ArithmeticRightShift<UIntArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: UIntArray, bitCount: Int): UIntArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * UInt.SIZE_BITS) return UIntArray(value.size) {
            // We shifted the whole value off
            if (value.last().toInt() < 0) UInt.MAX_VALUE else 0U
        }

        val offsetBytes = bitCount / UInt.SIZE_BITS
        val shiftMod = bitCount % UInt.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0U else UInt.MAX_VALUE shl (UInt.SIZE_BITS - shiftMod)
        val sign = value.last().toInt() < 0

        return UIntArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) UInt.MAX_VALUE else 0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] shr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (UInt.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UInt.MAX_VALUE and carryMask else 0U
                }
                src or dst
            }
        }
    }
}

interface LongArrayArithmeticRightShift : ArithmeticRightShift<LongArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: LongArray, bitCount: Int): LongArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * Long.SIZE_BITS) return LongArray(value.size) {
            // We shifted the whole value off
            if (value.last() < 0) ULong.MAX_VALUE.toLong() else 0L
        }

        val offsetBytes = bitCount / Long.SIZE_BITS
        val shiftMod = bitCount % Long.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0 else ULong.MAX_VALUE.toLong() shl (Long.SIZE_BITS - shiftMod)
        val sign = value.last() < 0

        return LongArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) ULong.MAX_VALUE.toLong() else 0L
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] ushr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (Long.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) ULong.MAX_VALUE.toLong() and carryMask else 0L
                }
                src or dst
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
interface ULongArrayArithmeticRightShift : ArithmeticRightShift<ULongArray> {
    @Suppress("detekt:ReturnCount")
    override fun arithmeticRightShift(value: ULongArray, bitCount: Int): ULongArray {
        require(bitCount >= 0) { "Negative right shifts are unsupported." }
        if (bitCount == 0) return value
        if (bitCount >= value.size * ULong.SIZE_BITS) return ULongArray(value.size) {
            // We shifted the whole value off
            if (value.last().toLong() < 0) ULong.MAX_VALUE else 0U
        }

        val offsetBytes = bitCount / ULong.SIZE_BITS
        val shiftMod = bitCount % ULong.SIZE_BITS
        val carryMask = if (shiftMod == 0) 0U else ULong.MAX_VALUE shl (ULong.SIZE_BITS - shiftMod)
        val sign = value.last().toLong() < 0

        return ULongArray(value.size) {
            val sourceIndex = it + offsetBytes
            if (sourceIndex >= value.size) {
                if (sign) ULong.MAX_VALUE else 0U
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (SIZE_BITS - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex] shr shiftMod
                val dst = if (sourceIndex + 1 < value.size) {
                    (value[sourceIndex + 1] shl (ULong.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) ULong.MAX_VALUE and carryMask else 0U
                }
                src or dst
            }
        }
    }
}
