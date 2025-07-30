package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Bitwise
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

object ByteBitwise : Bitwise<Byte> {
    override fun and(lhs: Byte, rhs: Byte): Byte = lhs and rhs
    override fun or(lhs: Byte, rhs: Byte): Byte = lhs or rhs
    override fun xor(lhs: Byte, rhs: Byte): Byte = lhs xor rhs
    override fun inv(value: Byte): Byte = value.inv()
}

object UByteBitwise : Bitwise<UByte> {
    override fun and(lhs: UByte, rhs: UByte): UByte = lhs and rhs
    override fun or(lhs: UByte, rhs: UByte): UByte = lhs or rhs
    override fun xor(lhs: UByte, rhs: UByte): UByte = lhs xor rhs
    override fun inv(value: UByte): UByte = value.inv()
}

object ShortBitwise : Bitwise<Short> {
    override fun and(lhs: Short, rhs: Short): Short = lhs and rhs
    override fun or(lhs: Short, rhs: Short): Short = lhs or rhs
    override fun xor(lhs: Short, rhs: Short): Short = lhs xor rhs
    override fun inv(value: Short): Short = value.inv()
}

object UShortBitwise : Bitwise<UShort> {
    override fun and(lhs: UShort, rhs: UShort): UShort = lhs and rhs
    override fun or(lhs: UShort, rhs: UShort): UShort = lhs or rhs
    override fun xor(lhs: UShort, rhs: UShort): UShort = lhs xor rhs
    override fun inv(value: UShort): UShort = value.inv()
}

object IntBitwise : Bitwise<Int> {
    override fun and(lhs: Int, rhs: Int): Int = lhs and rhs
    override fun or(lhs: Int, rhs: Int): Int = lhs or rhs
    override fun xor(lhs: Int, rhs: Int): Int = lhs xor rhs
    override fun inv(value: Int): Int = value.inv()
}

object UIntBitwise : Bitwise<UInt> {
    override fun and(lhs: UInt, rhs: UInt): UInt = lhs and rhs
    override fun or(lhs: UInt, rhs: UInt): UInt = lhs or rhs
    override fun xor(lhs: UInt, rhs: UInt): UInt = lhs xor rhs
    override fun inv(value: UInt): UInt = value.inv()
}

object LongBitwise : Bitwise<Long> {
    override fun and(lhs: Long, rhs: Long): Long = lhs and rhs
    override fun or(lhs: Long, rhs: Long): Long = lhs or rhs
    override fun xor(lhs: Long, rhs: Long): Long = lhs xor rhs
    override fun inv(value: Long): Long = value.inv()
}

object ULongBitwise : Bitwise<ULong> {
    override fun and(lhs: ULong, rhs: ULong): ULong = lhs and rhs
    override fun or(lhs: ULong, rhs: ULong): ULong = lhs or rhs
    override fun xor(lhs: ULong, rhs: ULong): ULong = lhs xor rhs
    override fun inv(value: ULong): ULong = value.inv()
}
