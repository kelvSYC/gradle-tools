package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Division

object ByteDivision : Division<Byte> {
    override fun divide(lhs: Byte, rhs: Byte): Byte = (lhs / rhs).toByte()
}

object UByteDivision : Division<UByte> {
    override fun divide(lhs: UByte, rhs: UByte): UByte = (lhs / rhs).toUByte()
}

object ShortDivision : Division<Short> {
    override fun divide(lhs: Short, rhs: Short): Short = (lhs / rhs).toShort()
}

object UShortDivision : Division<UShort> {
    override fun divide(lhs: UShort, rhs: UShort): UShort = (lhs / rhs).toUShort()
}

object IntDivision : Division<Int> {
    override fun divide(lhs: Int, rhs: Int): Int = lhs / rhs
}

object UIntDivision : Division<UInt> {
    override fun divide(lhs: UInt, rhs: UInt): UInt = lhs / rhs
}

object LongDivision : Division<Long> {
    override fun divide(lhs: Long, rhs: Long): Long = lhs / rhs
}

object ULongDivision : Division<ULong> {
    override fun divide(lhs: ULong, rhs: ULong): ULong = lhs / rhs
}

object FloatDivision : Division<Float> {
    override fun divide(lhs: Float, rhs: Float): Float = lhs / rhs
}

object DoubleDivision : Division<Double> {
    override fun divide(lhs: Double, rhs: Double): Double = lhs / rhs
}
