package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Multiplication

object ByteMultiplication : Multiplication<Byte> {
    // FIXME overflow on conversion from Int
    override fun multiply(lhs: Byte, rhs: Byte): Byte = (lhs * rhs).toByte()
}

object UByteMultiplication : Multiplication<UByte> {
    // FIXME overflow on conversion from Int
    override fun multiply(lhs: UByte, rhs: UByte): UByte = (lhs * rhs).toUByte()
}

object ShortMultiplication : Multiplication<Short> {
    // FIXME overflow on conversion from Int
    override fun multiply(lhs: Short, rhs: Short): Short = (lhs * rhs).toShort()
}

object UShortMultiplication : Multiplication<UShort> {
    // FIXME overflow on conversion from Int
    override fun multiply(lhs: UShort, rhs: UShort): UShort = (lhs * rhs).toUShort()
}

object IntMultiplication : Multiplication<Int> {
    override fun multiply(lhs: Int, rhs: Int): Int = lhs * rhs
}

object UIntMuliplication : Multiplication<UInt> {
    override fun multiply(lhs: UInt, rhs: UInt): UInt = lhs * rhs
}

object LongMultiplication : Multiplication<Long> {
    override fun multiply(lhs: Long, rhs: Long): Long = lhs * rhs
}

object ULongMultiplication : Multiplication<ULong> {
    override fun multiply(lhs: ULong, rhs: ULong): ULong = lhs * rhs
}

object FloatMultiplication : Multiplication<Float> {
    override fun multiply(lhs: Float, rhs: Float): Float = lhs * rhs
}

object DoubleMultiplication : Multiplication<Double> {
    override fun multiply(lhs: Double, rhs: Double): Double = lhs * rhs
}
