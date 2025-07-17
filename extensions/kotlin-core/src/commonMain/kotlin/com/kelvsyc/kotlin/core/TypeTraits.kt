package com.kelvsyc.kotlin.core

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.Byte as KByte
import kotlin.Double as KDouble
import kotlin.Float as KFloat
import kotlin.Int as KInt
import kotlin.Long as KLong
import kotlin.Short as KShort
import kotlin.UByte as KUByte
import kotlin.UInt as KUInt
import kotlin.ULong as KULong
import kotlin.UShort as KUShort

/**
 * Object holder for type traits for common Kotlin types.
 */
object TypeTraits {
    /**
     * Traits object for the [Byte][KByte] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Byte : BitCollection<KByte>,
        Addition<KByte>, Multiplication<KByte>, Bitwise<KByte>, BitShift<KByte>, BitRotate<KByte>, Signed<KByte> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KByte): Sequence<Boolean> = sequence {
            var mask = 1
            for (i in 0 ..< KByte.SIZE_BITS) {
                yield(value.toInt() and mask != 0)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KByte): Set<KInt> = buildSet {
            var mask = 1
            for (i in 0 ..< KByte.SIZE_BITS) {
                if (value.toInt() and mask != 0) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KByte): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KByte): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KByte, rhs: KByte): KByte = (lhs + rhs).toByte()
        override fun subtract(lhs: KByte, rhs: KByte): KByte = (lhs - rhs).toByte()

        override fun multiply(lhs: KByte, rhs: KByte): KByte = (lhs * rhs).toByte()
        override fun divide(lhs: KByte, rhs: KByte): KByte = (lhs / rhs).toByte()

        override fun and(lhs: KByte, rhs: KByte): KByte = lhs and rhs
        override fun or(lhs: KByte, rhs: KByte): KByte = lhs or rhs
        override fun xor(lhs: KByte, rhs: KByte): KByte = lhs xor rhs
        override fun inv(value: KByte): KByte = value.inv()

        override fun leftShift(value: KByte, bitCount: KInt): KByte = (value.toInt() shl bitCount).toByte()
        override fun rightShift(value: KByte, bitCount: KInt): KByte = (value.toUByte().toInt() ushr bitCount).toByte()
        override fun arithmeticRightShift(value: KByte, bitCount: KInt): KByte = (value.toInt() shr bitCount).toByte()

        override fun rotateLeft(value: KByte, bitCount: KInt): KByte = value.rotateLeft(bitCount)
        override fun rotateRight(value: KByte, bitCount: KInt): KByte = value.rotateRight(bitCount)

        override fun isPositive(value: KByte): Boolean = value > 0
        override fun isNegative(value: KByte): Boolean = value < 0
        override fun negate(value: KByte): KByte = (-value).toByte()
    }

    /**
     * Traits object for the [UByte][KUByte] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object UByte : BitCollection<KUByte>,
        Addition<KUByte>, Multiplication<KUByte>, Bitwise<KUByte>, BitShift<KUByte>, BitRotate<KUByte> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KUByte): Sequence<Boolean> = sequence {
            var mask = 1
            for (i in 0 ..< KUByte.SIZE_BITS) {
                yield(value.toInt() and mask != 0)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KUByte): Set<KInt> = buildSet {
            var mask = 1
            for (i in 0 ..< KUByte.SIZE_BITS) {
                if (value.toInt() and mask != 0) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KUByte): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KUByte): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KUByte, rhs: KUByte): KUByte = (lhs + rhs).toUByte()
        override fun subtract(lhs: KUByte, rhs: KUByte): KUByte = (lhs - rhs).toUByte()

        override fun multiply(lhs: KUByte, rhs: KUByte): KUByte = (lhs * rhs).toUByte()
        override fun divide(lhs: KUByte, rhs: KUByte): KUByte = (lhs / rhs).toUByte()

        override fun and(lhs: KUByte, rhs: KUByte): KUByte = lhs and rhs
        override fun or(lhs: KUByte, rhs: KUByte): KUByte = lhs or rhs
        override fun xor(lhs: KUByte, rhs: KUByte): KUByte = lhs xor rhs
        override fun inv(value: KUByte): KUByte = value.inv()

        override fun leftShift(value: KUByte, bitCount: KInt): KUByte = (value.toInt() shl bitCount).toUByte()
        override fun rightShift(value: KUByte, bitCount: KInt): KUByte = (value.toInt() ushr bitCount).toUByte()
        override fun arithmeticRightShift(value: KUByte, bitCount: KInt): KUByte = (value.toByte().toInt() shr bitCount).toUByte()

        override fun rotateLeft(value: KUByte, bitCount: KInt): KUByte = value.rotateLeft(bitCount)
        override fun rotateRight(value: KUByte, bitCount: KInt): KUByte = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [Short][KShort] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Short : BitCollection<KShort>,
        Addition<KShort>, Multiplication<KShort>, Bitwise<KShort>, BitShift<KShort>, BitRotate<KShort>, Signed<KShort> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KShort): Sequence<Boolean> = sequence {
            var mask = 1
            for (i in 0 ..< KShort.SIZE_BITS) {
                yield(value.toInt() and mask != 0)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KShort): Set<KInt> = buildSet {
            var mask = 1
            for (i in 0 ..< KShort.SIZE_BITS) {
                if (value.toInt() and mask != 0) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KShort): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KShort): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KShort, rhs: KShort): KShort = (lhs + rhs).toShort()
        override fun subtract(lhs: KShort, rhs: KShort): KShort = (lhs - rhs).toShort()

        override fun multiply(lhs: KShort, rhs: KShort): KShort = (lhs * rhs).toShort()
        override fun divide(lhs: KShort, rhs: KShort): KShort = (lhs / rhs).toShort()

        override fun and(lhs: KShort, rhs: KShort): KShort = lhs and rhs
        override fun or(lhs: KShort, rhs: KShort): KShort = lhs or rhs
        override fun xor(lhs: KShort, rhs: KShort): KShort = lhs xor rhs
        override fun inv(value: KShort): KShort = value.inv()

        override fun leftShift(value: KShort, bitCount: KInt): KShort = (value.toInt() shl bitCount).toShort()
        override fun rightShift(value: KShort, bitCount: KInt): KShort = (value.toUShort().toInt() ushr bitCount).toShort()
        override fun arithmeticRightShift(value: KShort, bitCount: KInt): KShort = (value.toInt() shr bitCount).toShort()

        override fun rotateLeft(value: KShort, bitCount: KInt): KShort = value.rotateLeft(bitCount)
        override fun rotateRight(value: KShort, bitCount: KInt): KShort = value.rotateRight(bitCount)

        override fun isPositive(value: KShort): Boolean = value > 0
        override fun isNegative(value: KShort): Boolean = value < 0
        override fun negate(value: KShort): KShort = (-value).toShort()
    }

    /**
     * Traits object for the [UShort][KUShort] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object UShort : BitCollection<KUShort>,
        Addition<KUShort>, Multiplication<KUShort>, Bitwise<KUShort>, BitShift<KUShort>, BitRotate<KUShort> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KUShort): Sequence<Boolean> = sequence {
            var mask = 1
            for (i in 0 ..< KUShort.SIZE_BITS) {
                yield(value.toInt() and mask != 0)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KUShort): Set<KInt> = buildSet {
            var mask = 1
            for (i in 0 ..< KUShort.SIZE_BITS) {
                if (value.toInt() and mask != 0) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KUShort): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KUShort): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KUShort, rhs: KUShort): KUShort = (lhs + rhs).toUShort()
        override fun subtract(lhs: KUShort, rhs: KUShort): KUShort = (lhs - rhs).toUShort()

        override fun multiply(lhs: KUShort, rhs: KUShort): KUShort = (lhs * rhs).toUShort()
        override fun divide(lhs: KUShort, rhs: KUShort): KUShort = (lhs / rhs).toUShort()

        override fun and(lhs: KUShort, rhs: KUShort): KUShort = lhs and rhs
        override fun or(lhs: KUShort, rhs: KUShort): KUShort = lhs or rhs
        override fun xor(lhs: KUShort, rhs: KUShort): KUShort = lhs xor rhs
        override fun inv(value: KUShort): KUShort = value.inv()

        override fun leftShift(value: KUShort, bitCount: KInt): KUShort = (value.toInt() shl bitCount).toUShort()
        override fun rightShift(value: KUShort, bitCount: KInt): KUShort = (value.toInt() ushr bitCount).toUShort()
        override fun arithmeticRightShift(value: KUShort, bitCount: KInt): KUShort = (value.toShort().toInt() shr bitCount).toUShort()

        override fun rotateLeft(value: KUShort, bitCount: KInt): KUShort = value.rotateLeft(bitCount)
        override fun rotateRight(value: KUShort, bitCount: KInt): KUShort = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [Int][KInt] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Int : BitCollection<KInt>,
        Addition<KInt>, Multiplication<KInt>, Bitwise<KInt>, BitShift<KInt>, BitRotate<KInt>, Signed<KInt> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KInt): Sequence<Boolean> = sequence {
            var mask = 1
            for (i in 0 ..< KInt.SIZE_BITS) {
                yield(value and mask != 0)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KInt): Set<KInt> = buildSet {
            var mask = 1
            for (i in 0 ..< KInt.SIZE_BITS) {
                if (value and mask != 0) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KInt): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KInt): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KInt, rhs: KInt): KInt = lhs + rhs
        override fun subtract(lhs: KInt, rhs: KInt): KInt = lhs - rhs

        override fun multiply(lhs: KInt, rhs: KInt): KInt = lhs * rhs
        override fun divide(lhs: KInt, rhs: KInt): KInt = lhs / rhs

        override fun and(lhs: KInt, rhs: KInt): KInt = lhs and rhs
        override fun or(lhs: KInt, rhs: KInt): KInt = lhs or rhs
        override fun xor(lhs: KInt, rhs: KInt): KInt = lhs xor rhs
        override fun inv(value: KInt): KInt = value.inv()

        override fun leftShift(value: KInt, bitCount: KInt): KInt = value shl bitCount
        override fun rightShift(value: KInt, bitCount: KInt): KInt = value ushr bitCount
        override fun arithmeticRightShift(value: KInt, bitCount: KInt): KInt = value shr bitCount

        override fun rotateLeft(value: KInt, bitCount: KInt): KInt = value.rotateLeft(bitCount)
        override fun rotateRight(value: KInt, bitCount: KInt): KInt = value.rotateRight(bitCount)

        override fun isPositive(value: KInt): Boolean = value > 0
        override fun isNegative(value: KInt): Boolean = value < 0
        override fun negate(value: KInt): KInt = -value
    }

    /**
     * Traits object for the [UInt][KUInt] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object UInt : BitCollection<KUInt>,
        Addition<KUInt>, Multiplication<KUInt>, Bitwise<KUInt>, BitShift<KUInt>, BitRotate<KUInt> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KUInt): Sequence<Boolean> = sequence {
            var mask = 1U
            for (i in 0 ..< KUInt.SIZE_BITS) {
                yield(value and mask != 0U)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KUInt): Set<KInt> = buildSet {
            var mask = 1U
            for (i in 0 ..< KUInt.SIZE_BITS) {
                if (value and mask != 0U) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KUInt): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KUInt): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KUInt, rhs: KUInt): KUInt = lhs + rhs
        override fun subtract(lhs: KUInt, rhs: KUInt): KUInt = lhs - rhs

        override fun multiply(lhs: KUInt, rhs: KUInt): KUInt = lhs * rhs
        override fun divide(lhs: KUInt, rhs: KUInt): KUInt = lhs / rhs

        override fun and(lhs: KUInt, rhs: KUInt): KUInt = lhs and rhs
        override fun or(lhs: KUInt, rhs: KUInt): KUInt = lhs or rhs
        override fun xor(lhs: KUInt, rhs: KUInt): KUInt = lhs xor rhs
        override fun inv(value: KUInt): KUInt = value.inv()

        override fun leftShift(value: KUInt, bitCount: KInt): KUInt = value shl bitCount
        override fun rightShift(value: KUInt, bitCount: KInt): KUInt = value shr bitCount
        override fun arithmeticRightShift(value: KUInt, bitCount: KInt): KUInt = (value.toInt() shr bitCount).toUInt()

        override fun rotateLeft(value: KUInt, bitCount: KInt): KUInt = value.rotateLeft(bitCount)
        override fun rotateRight(value: KUInt, bitCount: KInt): KUInt = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [Long][KLong] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Long : BitCollection<KLong>,
        Addition<KLong>, Multiplication<KLong>, Bitwise<KLong>, BitShift<KLong>, BitRotate<KLong>, Signed<KLong> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KLong): Sequence<Boolean> = sequence {
            var mask = 1L
            for (i in 0 ..< KLong.SIZE_BITS) {
                yield(value and mask != 0L)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KLong): Set<KInt> = buildSet {
            var mask = 1L
            for (i in 0 ..< KLong.SIZE_BITS) {
                if (value and mask != 0L) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KLong): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KLong): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KLong, rhs: KLong): KLong = lhs + rhs
        override fun subtract(lhs: KLong, rhs: KLong): KLong = lhs - rhs

        override fun multiply(lhs: KLong, rhs: KLong): KLong = lhs * rhs
        override fun divide(lhs: KLong, rhs: KLong): KLong = lhs / rhs

        override fun and(lhs: KLong, rhs: KLong): KLong = lhs and rhs
        override fun or(lhs: KLong, rhs: KLong): KLong = lhs or rhs
        override fun xor(lhs: KLong, rhs: KLong): KLong = lhs xor rhs
        override fun inv(value: KLong): KLong = value.inv()

        override fun leftShift(value: KLong, bitCount: KInt): KLong = value shl bitCount
        override fun rightShift(value: KLong, bitCount: KInt): KLong = value ushr bitCount
        override fun arithmeticRightShift(value: KLong, bitCount: KInt): KLong = value shr bitCount

        override fun rotateLeft(value: KLong, bitCount: KInt): KLong = value.rotateLeft(bitCount)
        override fun rotateRight(value: KLong, bitCount: KInt): KLong = value.rotateRight(bitCount)

        override fun isPositive(value: KLong): Boolean = value > 0
        override fun isNegative(value: KLong): Boolean = value < 0
        override fun negate(value: KLong): KLong = -value
    }

    /**
     * Traits object for the [ULong][KULong] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object ULong : BitCollection<KULong>,
        Addition<KULong>, Multiplication<KULong>, Bitwise<KULong>, BitShift<KULong>, BitRotate<KULong> {
        @OptIn(ExperimentalStdlibApi::class)
        override fun asBitSequence(value: KULong): Sequence<Boolean> = sequence {
            var mask = 1UL
            for (i in 0 ..< KULong.SIZE_BITS) {
                yield(value and mask != 0UL)
                mask = mask shl 1
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun getSetBits(value: KULong): Set<KInt> = buildSet {
            var mask = 1UL
            for (i in 0 ..< KULong.SIZE_BITS) {
                if (value and mask != 0UL) {
                    add(i)
                }
                mask = mask shl 1
            }
        }

        override fun countLeadingZeroBits(value: KULong): KInt = value.countLeadingZeroBits()
        override fun countTrailingZeroBits(value: KULong): KInt = value.countTrailingZeroBits()

        override fun add(lhs: KULong, rhs: KULong): KULong = lhs + rhs
        override fun subtract(lhs: KULong, rhs: KULong): KULong = lhs - rhs

        override fun multiply(lhs: KULong, rhs: KULong): KULong = lhs * rhs
        override fun divide(lhs: KULong, rhs: KULong): KULong = lhs / rhs

        override fun and(lhs: KULong, rhs: KULong): KULong = lhs and rhs
        override fun or(lhs: KULong, rhs: KULong): KULong = lhs or rhs
        override fun xor(lhs: KULong, rhs: KULong): KULong = lhs xor rhs
        override fun inv(value: KULong): KULong = value.inv()

        override fun leftShift(value: KULong, bitCount: KInt): KULong = value shl bitCount
        override fun rightShift(value: KULong, bitCount: KInt): KULong = value shr bitCount
        override fun arithmeticRightShift(value: KULong, bitCount: KInt): KULong = (value.toLong() shr bitCount).toULong()

        override fun rotateLeft(value: KULong, bitCount: KInt): KULong = value.rotateLeft(bitCount)
        override fun rotateRight(value: KULong, bitCount: KInt): KULong = value.rotateRight(bitCount)
    }

    object Float: Addition<KFloat>, Multiplication<KFloat>, Signed<KFloat> {
        override fun add(lhs: KFloat, rhs: KFloat): KFloat = lhs + rhs
        override fun subtract(lhs: KFloat, rhs: KFloat): KFloat = lhs - rhs

        override fun multiply(lhs: KFloat, rhs: KFloat): KFloat = lhs * rhs
        override fun divide(lhs: KFloat, rhs: KFloat): KFloat = lhs / rhs

        override fun isPositive(value: KFloat): Boolean = value > 0
        override fun isNegative(value: KFloat): Boolean = value < 0
        override fun negate(value: KFloat): KFloat = -value
    }

    object Double: Addition<KDouble>, Multiplication<KDouble>, Signed<KDouble> {
        override fun add(lhs: KDouble, rhs: KDouble): KDouble = lhs + rhs
        override fun subtract(lhs: KDouble, rhs: KDouble): KDouble = lhs - rhs

        override fun multiply(lhs: KDouble, rhs: KDouble): KDouble = lhs * rhs
        override fun divide(lhs: KDouble, rhs: KDouble): KDouble = lhs / rhs

        override fun isPositive(value: KDouble): Boolean = value > 0
        override fun isNegative(value: KDouble): Boolean = value < 0
        override fun negate(value: KDouble): KDouble = -value
    }
}
