package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.BitStoreConstants

object ByteBitStoreConstants : BitStoreConstants<Byte> {
    override val allClear: Byte = 0
    override val allSet: Byte = UByte.MAX_VALUE.toByte()
    override fun hasSetBits(value: Byte): Boolean = value != allClear
    override fun isAllClear(value: Byte): Boolean = value == allClear
}

object UByteBitStoreConstants : BitStoreConstants<UByte> {
    override val allClear: UByte = 0U
    override val allSet: UByte = UByte.MAX_VALUE
    override fun hasSetBits(value: UByte): Boolean = value != allClear
    override fun isAllClear(value: UByte): Boolean = value == allClear
}

object ShortBitStoreConstants : BitStoreConstants<Short> {
    override val allClear: Short = 0
    override val allSet: Short = UShort.MAX_VALUE.toShort()
    override fun hasSetBits(value: Short): Boolean = value != allClear
    override fun isAllClear(value: Short): Boolean = value == allClear
}

object UShortBitStoreConstants : BitStoreConstants<UShort> {
    override val allClear: UShort = 0U
    override val allSet: UShort = UShort.MAX_VALUE
    override fun hasSetBits(value: UShort): Boolean = value != allClear
    override fun isAllClear(value: UShort): Boolean = value == allClear
}

object IntBitStoreConstants : BitStoreConstants<Int> {
    override val allClear: Int = 0
    override val allSet: Int = UInt.MAX_VALUE.toInt()
    override fun hasSetBits(value: Int): Boolean = value != allClear
    override fun isAllClear(value: Int): Boolean = value == allClear
}

object UIntBitStoreConstants : BitStoreConstants<UInt> {
    override val allClear: UInt = 0U
    override val allSet: UInt = UInt.MAX_VALUE
    override fun hasSetBits(value: UInt): Boolean = value != allClear
    override fun isAllClear(value: UInt): Boolean = value == allClear
}

object LongBitStoreConstants : BitStoreConstants<Long> {
    override val allClear: Long = 0L
    override val allSet: Long = ULong.MAX_VALUE.toLong()
    override fun hasSetBits(value: Long): Boolean = value != allClear
    override fun isAllClear(value: Long): Boolean = value == allClear
}

object ULongBitStoreConstants : BitStoreConstants<ULong> {
    override val allClear: ULong = 0UL
    override val allSet: ULong = ULong.MAX_VALUE
    override fun hasSetBits(value: ULong): Boolean = value != allClear
    override fun isAllClear(value: ULong): Boolean = value == allClear
}
