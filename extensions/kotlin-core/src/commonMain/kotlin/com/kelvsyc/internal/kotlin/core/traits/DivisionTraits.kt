package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.ceilDiv
import com.kelvsyc.kotlin.core.traits.FloatingPointDivision
import com.kelvsyc.kotlin.core.traits.IntegerDivision

object ByteDivision : IntegerDivision<Byte> {
    override fun divide(lhs: Byte, rhs: Byte): Byte = (lhs / rhs).toByte()
    override fun floorDiv(lhs: Byte, rhs: Byte): Byte = lhs.floorDiv(rhs).toByte()
    override fun ceilDiv(lhs: Byte, rhs: Byte): Byte = lhs.ceilDiv(rhs).toByte()

    override fun rem(lhs: Byte, rhs: Byte): Byte = lhs.rem(rhs).toByte()
    override fun mod(lhs: Byte, rhs: Byte): Byte = lhs.mod(rhs)
}

object UByteDivision : IntegerDivision<UByte> {
    override fun divide(lhs: UByte, rhs: UByte): UByte = (lhs / rhs).toUByte()
    override fun floorDiv(lhs: UByte, rhs: UByte): UByte = lhs.floorDiv(rhs).toUByte()
    override fun ceilDiv(lhs: UByte, rhs: UByte): UByte = lhs.ceilDiv(rhs).toUByte()

    override fun rem(lhs: UByte, rhs: UByte): UByte = lhs.rem(rhs).toUByte()
    override fun mod(lhs: UByte, rhs: UByte): UByte = lhs.mod(rhs)
}

object ShortDivision : IntegerDivision<Short> {
    override fun divide(lhs: Short, rhs: Short): Short = (lhs / rhs).toShort()
    override fun floorDiv(lhs: Short, rhs: Short): Short = lhs.floorDiv(rhs).toShort()
    override fun ceilDiv(lhs: Short, rhs: Short): Short = lhs.ceilDiv(rhs).toShort()

    override fun rem(lhs: Short, rhs: Short): Short = lhs.rem(rhs).toShort()
    override fun mod(lhs: Short, rhs: Short): Short = lhs.mod(rhs)
}

object UShortDivision : IntegerDivision<UShort> {
    override fun divide(lhs: UShort, rhs: UShort): UShort = (lhs / rhs).toUShort()
    override fun floorDiv(lhs: UShort, rhs: UShort): UShort = lhs.floorDiv(rhs).toUShort()
    override fun ceilDiv(lhs: UShort, rhs: UShort): UShort = lhs.ceilDiv(rhs).toUShort()

    override fun rem(lhs: UShort, rhs: UShort): UShort = lhs.rem(rhs).toUShort()
    override fun mod(lhs: UShort, rhs: UShort): UShort = lhs.mod(rhs)
}

object IntDivision : IntegerDivision<Int> {
    override fun divide(lhs: Int, rhs: Int): Int = lhs / rhs
    override fun floorDiv(lhs: Int, rhs: Int): Int = lhs.floorDiv(rhs)
    override fun ceilDiv(lhs: Int, rhs: Int): Int = lhs.ceilDiv(rhs)

    override fun rem(lhs: Int, rhs: Int): Int = lhs.rem(rhs)
    override fun mod(lhs: Int, rhs: Int): Int = lhs.mod(rhs)
}

object UIntDivision : IntegerDivision<UInt> {
    override fun divide(lhs: UInt, rhs: UInt): UInt = lhs / rhs
    override fun floorDiv(lhs: UInt, rhs: UInt): UInt = lhs.floorDiv(rhs)
    override fun ceilDiv(lhs: UInt, rhs: UInt): UInt = lhs.ceilDiv(rhs)

    override fun rem(lhs: UInt, rhs: UInt): UInt = lhs.rem(rhs)
    override fun mod(lhs: UInt, rhs: UInt): UInt = lhs.mod(rhs)
}

object LongDivision : IntegerDivision<Long> {
    override fun divide(lhs: Long, rhs: Long): Long = lhs / rhs
    override fun floorDiv(lhs: Long, rhs: Long): Long = lhs.floorDiv(rhs)
    override fun ceilDiv(lhs: Long, rhs: Long): Long = lhs.ceilDiv(rhs)

    override fun rem(lhs: Long, rhs: Long): Long = lhs.rem(rhs)
    override fun mod(lhs: Long, rhs: Long): Long = lhs.mod(rhs)
}

object ULongDivision : IntegerDivision<ULong> {
    override fun divide(lhs: ULong, rhs: ULong): ULong = lhs / rhs
    override fun floorDiv(lhs: ULong, rhs: ULong): ULong = lhs.floorDiv(rhs)
    override fun ceilDiv(lhs: ULong, rhs: ULong): ULong = lhs.ceilDiv(rhs)

    override fun rem(lhs: ULong, rhs: ULong): ULong = lhs.rem(rhs)
    override fun mod(lhs: ULong, rhs: ULong): ULong = lhs.mod(rhs)
}

object FloatDivision : FloatingPointDivision<Float> {
    override fun divide(lhs: Float, rhs: Float): Float = lhs / rhs

    override fun rem(lhs: Float, rhs: Float): Float = lhs.rem(rhs)
    override fun mod(lhs: Float, rhs: Float): Float = lhs.mod(rhs)
}

object DoubleDivision : FloatingPointDivision<Double> {
    override fun divide(lhs: Double, rhs: Double): Double = lhs / rhs

    override fun rem(lhs: Double, rhs: Double): Double = lhs.rem(rhs)
    override fun mod(lhs: Double, rhs: Double): Double = lhs.mod(rhs)
}
