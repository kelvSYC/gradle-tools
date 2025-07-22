@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.core

import kotlin.math.absoluteValue
import kotlin.math.sign

fun Byte.ceilDiv(rhs: Byte): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Byte.ceilDiv(rhs: Short): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Byte.ceilDiv(rhs: Int): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Byte.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun Short.ceilDiv(rhs: Byte): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Short.ceilDiv(rhs: Short): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Short.ceilDiv(rhs: Int): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Short.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun Int.ceilDiv(rhs: Byte): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Int.ceilDiv(rhs: Short): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Int.ceilDiv(rhs: Int): Int = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Int.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue

fun Long.ceilDiv(rhs: Byte): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Long.ceilDiv(rhs: Short): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Long.ceilDiv(rhs: Int): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
fun Long.ceilDiv(rhs: Long): Long = floorDiv(rhs) + rem(rhs).sign.absoluteValue
