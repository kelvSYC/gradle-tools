package com.kelvsyc.kotlin.core

/**
 * Implementation of [BitShift] on [ByteArray], where the result of all operations return new byte arrays.
 */
object ByteArrayBitShift : BitShift<ByteArray> {
    override fun leftShift(value: ByteArray, bitCount: Int): ByteArray {
        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = (1 shl shiftMod) - 1

        val result = ByteArray(value.size)
        for (i in result.size -1 downTo 0) {
            val sourceIndex = i - offsetBytes
            if (sourceIndex < 0) {
                result[i] = 0
            } else {
                // src = shiftMod least significant bits from value[sourceIndex]
                // dst = (8 - shiftMod) most significant bits from value[sourceIndex - 1]
                val src = (value[sourceIndex].toInt() shl shiftMod)
                val dst = if (sourceIndex - 1 >= 0) {
                    (value[sourceIndex - 1].toInt() ushr (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = (src or dst).toByte()
            }
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun rightShift(value: ByteArray, bitCount: Int): ByteArray {
        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (Byte.SIZE_BITS - shiftMod)

        val result = ByteArray(value.size)
        for (i in 0 ..< result.size) {
            val sourceIndex = i + offsetBytes
            if (sourceIndex >= result.size) {
                result[i] = 0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (8 - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toUByte().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < result.size) {
                    (value[sourceIndex + 1].toInt() shl (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else 0
                result[i] = (src or dst).toByte()
            }
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun arithmeticRightShift(value: ByteArray, bitCount: Int): ByteArray {
        if (value.isEmpty()) return ByteArray(0)

        val offsetBytes = bitCount / Byte.SIZE_BITS
        val shiftMod = bitCount % Byte.SIZE_BITS
        val carryMask = UByte.MAX_VALUE.toInt() shl (Byte.SIZE_BITS - shiftMod)
        val sign = value.last() < 0

        val result = ByteArray(value.size)
        for (i in 0 ..< result.size) {
            val sourceIndex = i + offsetBytes
            if (sourceIndex >= result.size) {
                result[i] = if (sign) 0xFFU.toByte() else 0
            } else {
                // src = shiftMod most significant bits from value[sourceIndex]
                // dst = (8 - shiftMod) least significant bits from value[sourceIndex + 1]
                val src = value[sourceIndex].toUByte().toInt() ushr shiftMod
                val dst = if (sourceIndex + 1 < result.size) {
                    (value[sourceIndex + 1].toInt() shl (Byte.SIZE_BITS - shiftMod)) and carryMask
                } else {
                    if (sign) UByte.MAX_VALUE.toInt() and carryMask else 0
                }
                result[i] = (src or dst).toByte()
            }
        }
        return result
    }
}
