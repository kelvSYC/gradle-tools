@file:Suppress("detekt:TooManyFunctions")

package com.kelvsyc.kotlin.commons.lang

import org.apache.commons.lang3.Conversion

/**
 * Converts a binary value, represented as a [BooleanArray], to a [Byte], using the default byte and bit ordering.
 *
 * @see Conversion.binaryToByte
 */
fun BooleanArray.binaryToByte(srcPos: Int = 0, dstInit: Byte = 0, dstPos: Int = 0, nBools: Int = Byte.SIZE_BITS) =
    Conversion.binaryToByte(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a binary value, represented as a [BooleanArray], to a [Short], using the default byte and bit ordering.
 *
 * @see Conversion.binaryToShort
 */
fun BooleanArray.binaryToShort(srcPos: Int = 0, dstInit: Short = 0, dstPos: Int = 0, nBools: Int = Short.SIZE_BITS) =
    Conversion.binaryToShort(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a binary value, represented as a [BooleanArray], to an [Int], using the default byte and bit ordering.
 *
 * @see Conversion.binaryToInt
 */
fun BooleanArray.binaryToInt(srcPos: Int = 0, dstInit: Int = 0, dstPos: Int = 0, nBools: Int = Int.SIZE_BITS) =
    Conversion.binaryToInt(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a binary value, represented as a [BooleanArray], to a [Long], using the default byte and bit ordering.
 *
 * @see Conversion.binaryToLong
 */
fun BooleanArray.binaryToLong(srcPos: Int = 0, dstInit: Long = 0, dstPos:Int = 0, nBools: Int = Long.SIZE_BITS) =
    Conversion.binaryToLong(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a [ByteArray] to a [Short], using the default byte and bit ordering.
 *
 * @see Conversion.byteArrayToShort
 */
fun ByteArray.toShort(srcPos: Int = 0, dstInit: Short = 0, dstPos: Int = 0, nBytes:Int = Short.SIZE_BYTES) =
    Conversion.byteArrayToShort(this, srcPos, dstInit, dstPos, nBytes)

/**
 * Converts a [ByteArray] to an [Int], using the default byte and bit ordering.
 *
 * @see Conversion.byteArrayToInt
 */
fun ByteArray.toInt(srcPos: Int = 0, dstInit: Int = 0, dstPos: Int = 0, nBytes: Int = Int.SIZE_BYTES) =
    Conversion.byteArrayToInt(this, srcPos, dstInit, dstPos, nBytes)

/**
 * Converts a [ByteArray] to a [Long], using the default byte and bit ordering.
 *
 * @see Conversion.byteArrayToLong
 */
fun ByteArray.toLong(srcPos: Int = 0, dstInit: Long = 0, dstPos: Int = 0, nBytes: Int = Int.SIZE_BYTES) =
    Conversion.byteArrayToLong(this, srcPos, dstInit, dstPos, nBytes)

/**
 * Converts a [ShortArray] to an [Int], using the default byte and bit ordering.
 *
 * @see Conversion.shortArrayToInt
 */
fun ShortArray.toInt(srcPos: Int = 0, dstInit: Int = 0, dstPos: Int = 0, nShorts: Int = Int.SIZE_BYTES / Short.SIZE_BYTES) =
    Conversion.shortArrayToInt(this, srcPos, dstInit, dstPos, nShorts)

/**
 * Converts a [ShortArray] to an [Int], using the default byte and bit ordering.
 *
 * @see Conversion.shortArrayToLong
 */
fun ShortArray.toLong(srcPos: Int = 0, dstInit: Long = 0, dstPos: Int = 0, nShorts: Int = Long.SIZE_BYTES / Short.SIZE_BYTES) =
    Conversion.shortArrayToLong(this, srcPos, dstInit, dstPos, nShorts)

/**
 * Converts an [IntArray] to a [Long], using the default byte and bit ordering.
 *
 * @see Conversion.intArrayToLong
 */
fun IntArray.toLong(srcPos: Int = 0, dstInit: Long = 0, dstPos: Int = 0, nInts: Int = Long.SIZE_BYTES / Int.SIZE_BYTES) =
    Conversion.intArrayToLong(this, srcPos, dstInit, dstPos, nInts)

/**
 * Converts a [Byte] to a binary value, represented as a [BooleanArray], using the default byte and bit ordering.
 *
 * @see Conversion.byteToBinary
 */
fun Byte.toBinary(srcPos: Int = 0, dstInit: BooleanArray, dstPos: Int = 0, nBools: Int = Byte.SIZE_BITS) =
    Conversion.byteToBinary(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a [Short] to a binary value, represented as a [BooleanArray], using the default byte and bit ordering.
 *
 * @see Conversion.shortToBinary
 */
fun Short.toBinary(srcPos: Int = 0, dstInit: BooleanArray, dstPos: Int = 0, nBools: Int = Short.SIZE_BITS) =
    Conversion.shortToBinary(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a [Short] to a [ByteArray], using the default byte and bit ordering.
 *
 * @see Conversion.shortToByteArray
 */
fun Short.toByteArray(srcPos: Int = 0, dstInit: ByteArray, dstPos: Int = 0, nBytes: Int = Short.SIZE_BYTES) =
    Conversion.shortToByteArray(this, srcPos, dstInit, dstPos, nBytes)

/**
 * Converts an [Int] to a binary value, represented as a [BooleanArray], using the default byte and bit ordering.
 *
 * @see Conversion.intToBinary
 */
fun Int.toBinary(srcPos: Int = 0, dstInit: BooleanArray, dstPos: Int = 0, nBools: Int = Int.SIZE_BITS) =
    Conversion.intToBinary(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts an [Int] to a [ByteArray], using the default byte and bit ordering.
 *
 * @see Conversion.intToByteArray
 */
fun Int.toByteArray(srcPos: Int = 0, dstInit: ByteArray, dstPos: Int = 0, nBytes: Int = Int.SIZE_BYTES) =
    Conversion.intToByteArray(this, srcPos, dstInit, dstPos, nBytes)

/**
 * Converts an [Int] to a [ShortArray], using the default byte and bit ordering.
 *
 * @see Conversion.intToShortArray
 */
fun Int.toShortArray(srcPos: Int = 0, dstInit: ShortArray, dstPos: Int = 0, nShorts: Int = Int.SIZE_BYTES / Short.SIZE_BYTES) =
    Conversion.intToShortArray(this, srcPos, dstInit, dstPos, nShorts)

/**
 * Converts a [Long] to a binary value, represented as a [BooleanArray], using the default byte and bit ordering.
 *
 * @see Conversion.longToBinary
 */
fun Long.toBinary(srcPos: Int = 0, dstInit: BooleanArray, dstPos: Int = 0, nBools: Int = Long.SIZE_BITS) =
    Conversion.longToBinary(this, srcPos, dstInit, dstPos, nBools)

/**
 * Converts a [Long] to a [ByteArray], using the default byte and bit ordering.
 *
 * @see Conversion.longToByteArray
 */
fun Long.toByteArray(srcPos: Int = 0, dstInit: ByteArray, dstPos: Int = 0, nBytes: Int = Long.SIZE_BYTES) =
    Conversion.longToByteArray(this, srcPos, dstInit, dstPos, nBytes)

/**
 * Converts a [Long] to a [ShortArray], using the default byte and bit ordering.
 *
 * @see Conversion.longToShortArray
 */
fun Long.toShortArray(srcPos: Int = 0, dstInit: ShortArray, dstPos: Int = 0, nShorts: Int = Long.SIZE_BYTES / Short.SIZE_BYTES) =
    Conversion.longToShortArray(this, srcPos, dstInit, dstPos, nShorts)

/**
 * Converts a [Long] to an [IntArray], using the default byte and bit ordering.
 *
 * @see Conversion.longToIntArray
 */
fun Long.toIntArray(srcPos: Int = 0, dstInit: IntArray, dstPos: Int = 0, nInts: Int = Long.SIZE_BYTES / Int.SIZE_BYTES) =
    Conversion.longToIntArray(this, srcPos, dstInit, dstPos, nInts)
