@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.core

import kotlin.math.absoluteValue
import kotlin.math.sign

fun Byte.ceilDiv(rhs: Byte): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Byte.ceilDiv(rhs: Short): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Byte.ceilDiv(rhs: Int): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Byte.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun UByte.ceilDiv(rhs: UByte): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UByte.ceilDiv(rhs: UShort): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UByte.ceilDiv(rhs: UInt): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UByte.ceilDiv(rhs: ULong): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL

fun Short.ceilDiv(rhs: Byte): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Short.ceilDiv(rhs: Short): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Short.ceilDiv(rhs: Int): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Short.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun UShort.ceilDiv(rhs: UByte): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UShort.ceilDiv(rhs: UShort): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UShort.ceilDiv(rhs: UInt): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UShort.ceilDiv(rhs: ULong): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL

fun Int.ceilDiv(rhs: Byte): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Int.ceilDiv(rhs: Short): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Int.ceilDiv(rhs: Int): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Int.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun UInt.ceilDiv(rhs: UByte): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UInt.ceilDiv(rhs: UShort): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UInt.ceilDiv(rhs: UInt): UInt = floorDiv(rhs) + if (rem(rhs) != 0U) 1U else 0U
fun UInt.ceilDiv(rhs: ULong): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL

fun Long.ceilDiv(rhs: Byte): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Long.ceilDiv(rhs: Short): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Long.ceilDiv(rhs: Int): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Long.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun ULong.ceilDiv(rhs: UByte): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL
fun ULong.ceilDiv(rhs: UShort): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL
fun ULong.ceilDiv(rhs: UInt): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL
fun ULong.ceilDiv(rhs: ULong): ULong = floorDiv(rhs) + if (rem(rhs) != 0UL) 1UL else 0UL
