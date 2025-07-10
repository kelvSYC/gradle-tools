package com.kelvsyc.kotlin.core

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.Byte as KByte
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
    object Byte : Bitwise<KByte>, BitShift<KByte>, BitRotate<KByte> {
        override fun and(lhs: KByte, rhs: KByte): KByte = lhs and rhs
        override fun or(lhs: KByte, rhs: KByte): KByte = lhs or rhs
        override fun xor(lhs: KByte, rhs: KByte): KByte = lhs xor rhs
        override fun inv(value: KByte): KByte = value.inv()

        override fun leftShift(value: KByte, bitCount: KInt): KByte = (value.toInt() shl bitCount).toByte()
        override fun rightShift(value: KByte, bitCount: KInt): KByte = (value.toUByte().toInt() ushr bitCount).toByte()
        override fun arithmeticRightShift(value: KByte, bitCount: KInt): KByte = (value.toInt() shr bitCount).toByte()

        override fun rotateLeft(value: KByte, bitCount: KInt): KByte = value.rotateLeft(bitCount)
        override fun rotateRight(value: KByte, bitCount: KInt): KByte = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [UByte][KUByte] type.
     */
    object UByte : Bitwise<KUByte>, BitShift<KUByte>, BitRotate<KUByte> {
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
    object Short : Bitwise<KShort>, BitShift<KShort>, BitRotate<KShort> {
        override fun and(lhs: KShort, rhs: KShort): KShort = lhs and rhs
        override fun or(lhs: KShort, rhs: KShort): KShort = lhs or rhs
        override fun xor(lhs: KShort, rhs: KShort): KShort = lhs xor rhs
        override fun inv(value: KShort): KShort = value.inv()

        override fun leftShift(value: KShort, bitCount: KInt): KShort = (value.toInt() shl bitCount).toShort()
        override fun rightShift(value: KShort, bitCount: KInt): KShort = (value.toUShort().toInt() ushr bitCount).toShort()
        override fun arithmeticRightShift(value: KShort, bitCount: KInt): KShort = (value.toInt() shr bitCount).toShort()

        override fun rotateLeft(value: KShort, bitCount: KInt): KShort = value.rotateLeft(bitCount)
        override fun rotateRight(value: KShort, bitCount: KInt): KShort = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [UShort][KUShort] type.
     */
    object UShort : Bitwise<KUShort>, BitShift<KUShort>, BitRotate<KUShort> {
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
    object Int : Bitwise<KInt>, BitShift<KInt>, BitRotate<KInt> {
        override fun and(lhs: KInt, rhs: KInt): KInt = lhs and rhs
        override fun or(lhs: KInt, rhs: KInt): KInt = lhs or rhs
        override fun xor(lhs: KInt, rhs: KInt): KInt = lhs xor rhs
        override fun inv(value: KInt): KInt = value.inv()

        override fun leftShift(value: KInt, bitCount: KInt): KInt = value shl bitCount
        override fun rightShift(value: KInt, bitCount: KInt): KInt = value ushr bitCount
        override fun arithmeticRightShift(value: KInt, bitCount: KInt): KInt = value shr bitCount

        override fun rotateLeft(value: KInt, bitCount: KInt): KInt = value.rotateLeft(bitCount)
        override fun rotateRight(value: KInt, bitCount: KInt): KInt = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [UInt][KUInt] type.
     */
    object UInt : Bitwise<KUInt>, BitShift<KUInt>, BitRotate<KUInt> {
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
    object Long : Bitwise<KLong>, BitShift<KLong>, BitRotate<KLong> {
        override fun and(lhs: KLong, rhs: KLong): KLong = lhs and rhs
        override fun or(lhs: KLong, rhs: KLong): KLong = lhs or rhs
        override fun xor(lhs: KLong, rhs: KLong): KLong = lhs xor rhs
        override fun inv(value: KLong): KLong = value.inv()

        override fun leftShift(value: KLong, bitCount: KInt): KLong = value shl bitCount
        override fun rightShift(value: KLong, bitCount: KInt): KLong = value ushr bitCount
        override fun arithmeticRightShift(value: KLong, bitCount: KInt): KLong = value shr bitCount

        override fun rotateLeft(value: KLong, bitCount: KInt): KLong = value.rotateLeft(bitCount)
        override fun rotateRight(value: KLong, bitCount: KInt): KLong = value.rotateRight(bitCount)
    }

    /**
     * Traits object for the [ULong][KULong] type.
     */
    object ULong : Bitwise<KULong>, BitShift<KULong>, BitRotate<KULong> {
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
}
