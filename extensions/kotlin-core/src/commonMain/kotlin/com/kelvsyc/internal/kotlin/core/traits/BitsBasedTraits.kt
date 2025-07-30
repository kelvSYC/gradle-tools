package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.Converter
import com.kelvsyc.kotlin.core.traits.BitsBased

object ByteBitsBased : BitsBased<Byte, Byte> {
    override val converter: Converter<Byte, Byte> = Converter.identity()
}

object UByteBitsBased : BitsBased<UByte, Byte> {
    override val converter: Converter<UByte, Byte> = Converter.of(UByte::toByte, Byte::toUByte)
}

object ShortBitsBased : BitsBased<Short, Short> {
    override val converter: Converter<Short, Short> = Converter.identity()
}

object UShortBitsBased : BitsBased<UShort, Short> {
    override val converter: Converter<UShort, Short> = Converter.of(UShort::toShort, Short::toUShort)
}

object IntBitsBased : BitsBased<Int, Int> {
    override val converter: Converter<Int, Int> = Converter.identity()
}

object UIntBitsBased : BitsBased<UInt, Int> {
    override val converter: Converter<UInt, Int> = Converter.of(UInt::toInt, Int::toUInt)
}

object LongBitsBased: BitsBased<Long, Long> {
    override val converter: Converter<Long, Long> = Converter.identity()
}

object ULongBitsBased : BitsBased<ULong, Long> {
    override val converter: Converter<ULong, Long> = Converter.of(ULong::toLong, Long::toULong)
}

object FloatBitsBased : BitsBased<Float, Int> {
    override val converter: Converter<Float, Int> = Converter.of(Float::toRawBits, Float::fromBits)
}

object DoubleBitsBased : BitsBased<Double, Long> {
    override val converter: Converter<Double, Long> = Converter.of(Double::toRawBits, Double::fromBits)
}
