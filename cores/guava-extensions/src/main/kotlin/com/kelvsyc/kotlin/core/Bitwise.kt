package com.kelvsyc.kotlin.core

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

/**
 * Interface denoting that a type supports bitwise operations.
 *
 * Note that this interface does not impose the standard infix operators (`and`, `or`, `shl`, etc.) on the type. This
 * is due to types like [UInt] may have nonstandard implementations of operators that makes sense for their type in
 * isolation (For example, [UInt.shr] is a logical shift, equivalent to [Int.ushr].) and/or where bitwise operations
 * may not inherently make sense.
 *
 * @param T The type supporting bitwise operations.
 */
interface Bitwise<T> {
    companion object {
        /**
         * Implementation of [Bitwise] for [Byte].
         */
        object ByteBitwise: Bitwise<Byte> {
            override fun and(lhs: Byte, rhs: Byte): Byte = lhs and rhs
            override fun or(lhs: Byte, rhs: Byte): Byte = lhs or rhs
            override fun xor(lhs: Byte, rhs: Byte): Byte = lhs xor rhs
            override fun inv(value: Byte): Byte = value.inv()

            override fun leftShift(value: Byte, bitCount: Int): Byte = (value.toInt() shl bitCount).toByte()
            override fun arithmeticRightShift(value: Byte, bitCount: Int): Byte = (value.toInt() shr bitCount).toByte()
            override fun logicalRightShift(value: Byte, bitCount: Int): Byte = (value.toUByte().toInt() ushr bitCount).toByte()

            override fun rotateLeft(value: Byte, bitCount: Int): Byte = value.rotateLeft(bitCount)
            override fun rotateRight(value: Byte, bitCount: Int): Byte = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [UByte].
         */
        object UByteBitwise : Bitwise<UByte> {
            override fun and(lhs: UByte, rhs: UByte): UByte = lhs and rhs
            override fun or(lhs: UByte, rhs: UByte): UByte = lhs or rhs
            override fun xor(lhs: UByte, rhs: UByte): UByte = lhs xor rhs
            override fun inv(value: UByte): UByte = value.inv()

            override fun leftShift(value: UByte, bitCount: Int): UByte = (value.toInt() shl bitCount).toUByte()
            override fun arithmeticRightShift(value: UByte, bitCount: Int): UByte = (value.toByte().toInt() shr bitCount).toUByte()
            override fun logicalRightShift(value: UByte, bitCount: Int): UByte = (value.toUInt() shr bitCount).toUByte()

            override fun rotateLeft(value: UByte, bitCount: Int): UByte = value.rotateLeft(bitCount)
            override fun rotateRight(value: UByte, bitCount: Int): UByte = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [Short].
         */
        object ShortBitwise: Bitwise<Short> {
            override fun and(lhs: Short, rhs: Short): Short = lhs and rhs
            override fun or(lhs: Short, rhs: Short): Short = lhs or rhs
            override fun xor(lhs: Short, rhs: Short): Short = lhs xor rhs
            override fun inv(value: Short): Short = value.inv()

            override fun leftShift(value: Short, bitCount: Int): Short = (value.toInt() shl bitCount).toShort()
            override fun arithmeticRightShift(value: Short, bitCount: Int): Short = (value.toInt() shr bitCount).toShort()
            override fun logicalRightShift(value: Short, bitCount: Int): Short = (value.toUShort().toInt() shr bitCount).toShort()

            override fun rotateLeft(value: Short, bitCount: Int): Short = value.rotateLeft(bitCount)
            override fun rotateRight(value: Short, bitCount: Int): Short = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [UShort].
         */
        object UShortBitwise: Bitwise<UShort> {
            override fun and(lhs: UShort, rhs: UShort): UShort = lhs and rhs
            override fun or(lhs: UShort, rhs: UShort): UShort = lhs or rhs
            override fun xor(lhs: UShort, rhs: UShort): UShort = lhs xor rhs
            override fun inv(value: UShort): UShort = value.inv()

            override fun leftShift(value: UShort, bitCount: Int): UShort = (value.toInt() shl bitCount).toUShort()
            override fun arithmeticRightShift(value: UShort, bitCount: Int): UShort = (value.toShort().toInt() shr bitCount).toUShort()
            override fun logicalRightShift(value: UShort, bitCount: Int): UShort = (value.toUInt() shr bitCount).toUShort()

            override fun rotateLeft(value: UShort, bitCount: Int): UShort = value.rotateLeft(bitCount)
            override fun rotateRight(value: UShort, bitCount: Int): UShort = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [Int].
         */
        object IntBitwise : Bitwise<Int> {
            override fun and(lhs: Int, rhs: Int): Int = lhs and rhs
            override fun or(lhs: Int, rhs: Int): Int = lhs or rhs
            override fun xor(lhs: Int, rhs: Int): Int = lhs xor rhs
            override fun inv(value: Int): Int = value.inv()

            override fun leftShift(value: Int, bitCount: Int): Int = value shl bitCount
            override fun arithmeticRightShift(value: Int, bitCount: Int): Int = value shr bitCount
            override fun logicalRightShift(value: Int, bitCount: Int): Int = value ushr bitCount

            override fun rotateLeft(value: Int, bitCount: Int): Int = value.rotateLeft(bitCount)
            override fun rotateRight(value: Int, bitCount: Int): Int = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [UInt].
         */
        object UIntBitwise : Bitwise<UInt> {
            override fun and(lhs: UInt, rhs: UInt): UInt = lhs and rhs
            override fun or(lhs: UInt, rhs: UInt): UInt = lhs or rhs
            override fun xor(lhs: UInt, rhs: UInt): UInt = lhs xor rhs
            override fun inv(value: UInt): UInt = value.inv()

            override fun leftShift(value: UInt, bitCount: Int): UInt = value shl bitCount
            override fun arithmeticRightShift(value: UInt, bitCount: Int): UInt = (value.toInt() shr bitCount).toUInt()
            override fun logicalRightShift(value: UInt, bitCount: Int): UInt = value shr bitCount

            override fun rotateLeft(value: UInt, bitCount: Int): UInt = value.rotateLeft(bitCount)
            override fun rotateRight(value: UInt, bitCount: Int): UInt = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [Long].
         */
        object LongBitwise: Bitwise<Long> {
            override fun and(lhs: Long, rhs: Long): Long = lhs and rhs
            override fun or(lhs: Long, rhs: Long): Long = lhs or rhs
            override fun xor(lhs: Long, rhs: Long): Long = lhs xor rhs
            override fun inv(value: Long): Long = value.inv()

            override fun leftShift(value: Long, bitCount: Int): Long = value shl bitCount
            override fun arithmeticRightShift(value: Long, bitCount: Int): Long = value shr bitCount
            override fun logicalRightShift(value: Long, bitCount: Int): Long = value ushr bitCount

            override fun rotateLeft(value: Long, bitCount: Int): Long = value.rotateLeft(bitCount)
            override fun rotateRight(value: Long, bitCount: Int): Long = value.rotateRight(bitCount)
        }

        /**
         * Implementation of [Bitwise] for [ULong].
         */
        object ULongBitwise: Bitwise<ULong> {
            override fun and(lhs: ULong, rhs: ULong): ULong = lhs and rhs
            override fun or(lhs: ULong, rhs: ULong): ULong = lhs or rhs
            override fun xor(lhs: ULong, rhs: ULong): ULong = lhs xor rhs
            override fun inv(value: ULong): ULong = value.inv()

            override fun leftShift(value: ULong, bitCount: Int): ULong = value shl bitCount
            override fun arithmeticRightShift(value: ULong, bitCount: Int): ULong = (value.toLong() shr bitCount).toULong()
            override fun logicalRightShift(value: ULong, bitCount: Int): ULong = value shr bitCount

            override fun rotateLeft(value: ULong, bitCount: Int): ULong = value.rotateLeft(bitCount)
            override fun rotateRight(value: ULong, bitCount: Int): ULong = value.rotateRight(bitCount)
        }
    }

    fun and(lhs: T, rhs: T): T
    fun or(lhs: T, rhs: T): T
    fun xor(lhs: T, rhs: T): T
    fun inv(value: T): T

    /**
     * Performs a left shift of the value.
     *
     * The behavior of a negative shift, or a shift in excess of [sizeBits] is undefined.
     */
    fun leftShift(value: T, bitCount: Int): T

    /**
     * Performs an arithmetic (signed) right shift of the value.
     *
     * The behavior of a negative shift, or a shift in excess of [sizeBits] is undefined.
     */
    fun arithmeticRightShift(value: T, bitCount: Int): T

    /**
     * Performs a logical (unsigned) right shift of the value.
     *
     * The behavior of a negative shift, or a shift in excess of [sizeBits] is undefined.
     */
    fun logicalRightShift(value: T, bitCount: Int): T

    /**
     * Performs a left rotation of this value. If a negative rotation is specified, a right rotation will be performed
     * instead. Rotating by a value in excess of [sizeBits] is considered the same as rotating by the value, modulo
     * [sizeBits].
     */
    fun rotateLeft(value: T, bitCount: Int): T

    /**
     * Performs a right rotation of this value. If a negative rotation is specified, a left rotation will be performed
     * instead. Rotating by a value in excess of [sizeBits] is considered the same as rotating by the value, modulo
     * [sizeBits].
     */
    fun rotateRight(value: T, bitCount: Int): T
}
