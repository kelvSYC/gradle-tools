package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.IntegralConstants

object ByteIntegralConstants : IntegralConstants<Byte> {
    override val zero: Byte = 0
    override val minValue: Byte = Byte.MIN_VALUE
    override val maxValue: Byte = Byte.MAX_VALUE
    override fun isZero(value: Byte): Boolean = value == 0.toByte()
}

object UByteIntegralConstants : IntegralConstants<UByte> {
    override val zero: UByte = 0U
    override val minValue: UByte = UByte.MIN_VALUE
    override val maxValue: UByte = UByte.MAX_VALUE
    override fun isZero(value: UByte): Boolean = value == 0U.toUByte()
}

object ShortIntegralConstants : IntegralConstants<Short> {
    override val zero: Short = 0
    override val minValue: Short = Short.MIN_VALUE
    override val maxValue: Short = Short.MAX_VALUE
    override fun isZero(value: Short): Boolean = value == 0.toShort()
}

object UShortIntegralConstants : IntegralConstants<UShort> {
    override val zero: UShort = 0U
    override val minValue: UShort = UShort.MIN_VALUE
    override val maxValue: UShort = UShort.MAX_VALUE
    override fun isZero(value: UShort): Boolean = value == 0U.toUShort()
}

object IntIntegralConstants : IntegralConstants<Int> {
    override val zero: Int = 0
    override val minValue: Int = Int.MIN_VALUE
    override val maxValue: Int = Int.MAX_VALUE
    override fun isZero(value: Int): Boolean = value == 0
}

object UIntIntegralConstants : IntegralConstants<UInt> {
    override val zero: UInt = 0U
    override val minValue: UInt = UInt.MIN_VALUE
    override val maxValue: UInt = UInt.MAX_VALUE
    override fun isZero(value: UInt): Boolean = value == 0U
}

object LongIntegralConstants : IntegralConstants<Long> {
    override val zero: Long = 0L
    override val minValue: Long = Long.MIN_VALUE
    override val maxValue: Long = Long.MAX_VALUE
    override fun isZero(value: Long): Boolean = value == 0L
}

object ULongIntegralConstants : IntegralConstants<ULong> {
    override val zero: ULong = 0UL
    override val minValue: ULong = ULong.MIN_VALUE
    override val maxValue: ULong = ULong.MAX_VALUE
    override fun isZero(value: ULong): Boolean = value == 0UL
}
