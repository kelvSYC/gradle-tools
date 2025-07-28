package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import com.kelvsyc.kotlin.core.traits.BitRotate

interface ByteBitRotate : BitRotate<Byte> {
    override fun rotateLeft(value: Byte, bitCount: Int): Byte = value.rotateLeft(bitCount)
    override fun rotateRight(value: Byte, bitCount: Int): Byte = value.rotateRight(bitCount)
}

interface UByteBitRotate : BitRotate<UByte> {
    override fun rotateLeft(value: UByte, bitCount: Int): UByte = value.rotateLeft(bitCount)
    override fun rotateRight(value: UByte, bitCount: Int): UByte = value.rotateRight(bitCount)
}

interface ShortBitRotate : BitRotate<Short> {
    override fun rotateLeft(value: Short, bitCount: Int): Short = value.rotateLeft(bitCount)
    override fun rotateRight(value: Short, bitCount: Int): Short = value.rotateRight(bitCount)
}

interface UShortBitRotate : BitRotate<UShort> {
    override fun rotateLeft(value: UShort, bitCount: Int): UShort = value.rotateLeft(bitCount)
    override fun rotateRight(value: UShort, bitCount: Int): UShort = value.rotateRight(bitCount)
}

interface IntBitRotate : BitRotate<Int> {
    override fun rotateLeft(value: Int, bitCount: Int): Int = value.rotateLeft(bitCount)
    override fun rotateRight(value: Int, bitCount: Int): Int = value.rotateRight(bitCount)
}

interface UIntBitRotate : BitRotate<UInt> {
    override fun rotateLeft(value: UInt, bitCount: Int): UInt = value.rotateLeft(bitCount)
    override fun rotateRight(value: UInt, bitCount: Int): UInt = value.rotateRight(bitCount)
}

interface LongBitRotate : BitRotate<Long> {
    override fun rotateLeft(value: Long, bitCount: Int): Long = value.rotateLeft(bitCount)
    override fun rotateRight(value: Long, bitCount: Int): Long = value.rotateRight(bitCount)
}

interface ULongBitRotate : BitRotate<ULong> {
    override fun rotateLeft(value: ULong, bitCount: Int): ULong = value.rotateLeft(bitCount)
    override fun rotateRight(value: ULong, bitCount: Int): ULong = value.rotateRight(bitCount)
}

interface ByteArrayBitRotate : BitRotate<ByteArray> {
    override fun rotateLeft(value: ByteArray, bitCount: Int): ByteArray {
        val base = TypeTraits.ByteArray
        val sizeBits = value.size * Byte.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            ByteArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            ByteArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: ByteArray, bitCount: Int): ByteArray = rotateLeft(value, -bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UByteArrayBitRotate : BitRotate<UByteArray> {
    override fun rotateLeft(value: UByteArray, bitCount: Int): UByteArray {
        val base = TypeTraits.UByteArray
        val sizeBits = value.size * UByte.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            UByteArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            UByteArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: UByteArray, bitCount: Int): UByteArray = rotateLeft(value, -bitCount)
}

interface ShortArrayBitRotate : BitRotate<ShortArray> {
    override fun rotateLeft(value: ShortArray, bitCount: Int): ShortArray {
        val base = TypeTraits.ShortArray
        val sizeBits = value.size * Short.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            ShortArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            ShortArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: ShortArray, bitCount: Int): ShortArray = rotateLeft(value, -bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UShortArrayBitRotate : BitRotate<UShortArray> {
    override fun rotateLeft(value: UShortArray, bitCount: Int): UShortArray {
        val base = TypeTraits.UShortArray
        val sizeBits = value.size * UShort.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            UShortArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            UShortArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: UShortArray, bitCount: Int): UShortArray = rotateLeft(value, -bitCount)
}

interface IntArrayBitRotate : BitRotate<IntArray> {
    override fun rotateLeft(value: IntArray, bitCount: Int): IntArray {
        val base = TypeTraits.IntArray
        val sizeBits = value.size * Int.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            IntArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            IntArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: IntArray, bitCount: Int): IntArray = rotateLeft(value, -bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface UIntArrayBitRotate : BitRotate<UIntArray> {
    override fun rotateLeft(value: UIntArray, bitCount: Int): UIntArray {
        val base = TypeTraits.UIntArray
        val sizeBits = value.size * UInt.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            UIntArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            UIntArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: UIntArray, bitCount: Int): UIntArray = rotateLeft(value, -bitCount)
}

interface LongArrayBitRotate : BitRotate<LongArray> {
    override fun rotateLeft(value: LongArray, bitCount: Int): LongArray {
        val base = TypeTraits.LongArray
        val sizeBits = value.size * Long.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            LongArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            LongArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: LongArray, bitCount: Int): LongArray = rotateLeft(value, -bitCount)
}

@OptIn(ExperimentalUnsignedTypes::class)
interface ULongArrayBitRotate : BitRotate<ULongArray> {
    override fun rotateLeft(value: ULongArray, bitCount: Int): ULongArray {
        val base = TypeTraits.ULongArray
        val sizeBits = value.size * ULong.SIZE_BITS
        val trueBitCount = bitCount.rem(sizeBits)
        return if (trueBitCount == 0) {
            value
        } else if (trueBitCount > 0) {
            val left = base.leftShift(value, trueBitCount)
            val right = base.rightShift(value, sizeBits - trueBitCount)
            ULongArrayBitwise().or(left, right) // TODO Fix
        } else {
            val left = base.leftShift(value, sizeBits + trueBitCount)
            val right = base.rightShift(value, -trueBitCount)
            ULongArrayBitwise().or(left, right) // TODO Fix
        }
    }
    override fun rotateRight(value: ULongArray, bitCount: Int): ULongArray = rotateLeft(value, -bitCount)
}
