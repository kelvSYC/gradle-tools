package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Addition

object ByteAddition : Addition<Byte> {
    override fun add(lhs: Byte, rhs: Byte): Byte = (lhs + rhs).toByte()
    override fun subtract(lhs: Byte, rhs: Byte): Byte = (lhs - rhs).toByte()
}

object UByteAddition : Addition<UByte> {
    override fun add(lhs: UByte, rhs: UByte): UByte = (lhs + rhs).toUByte()
    override fun subtract(lhs: UByte, rhs: UByte): UByte = (lhs - rhs).toUByte()
}

object ShortAddition : Addition<Short> {
    override fun add(lhs: Short, rhs: Short): Short = (lhs + rhs).toShort()
    override fun subtract(lhs: Short, rhs: Short): Short = (lhs - rhs).toShort()
}

object UShortAddition : Addition<UShort> {
    override fun add(lhs: UShort, rhs: UShort): UShort = (lhs + rhs).toUShort()
    override fun subtract(lhs: UShort, rhs: UShort): UShort = (lhs - rhs).toUShort()
}

object IntAddition : Addition<Int> {
    override fun add(lhs: Int, rhs: Int): Int = lhs + rhs
    override fun subtract(lhs: Int, rhs: Int): Int = lhs - rhs
}

object UIntAddition : Addition<UInt> {
    override fun add(lhs: UInt, rhs: UInt): UInt = lhs + rhs
    override fun subtract(lhs: UInt, rhs: UInt): UInt = lhs - rhs
}

object LongAddition : Addition<Long> {
    override fun add(lhs: Long, rhs: Long): Long = lhs + rhs
    override fun subtract(lhs: Long, rhs: Long): Long = lhs - rhs
}

object ULongAddition : Addition<ULong> {
    override fun add(lhs: ULong, rhs: ULong): ULong = lhs + rhs
    override fun subtract(lhs: ULong, rhs: ULong): ULong = lhs - rhs
}

object FloatAddition : Addition<Float> {
    override fun add(lhs: Float, rhs: Float): Float = lhs + rhs
    override fun subtract(lhs: Float, rhs: Float): Float = lhs - rhs
}

object DoubleAddition : Addition<Double> {
    override fun add(lhs: Double, rhs: Double): Double = lhs + rhs
    override fun subtract(lhs: Double, rhs: Double): Double = lhs - rhs
}
