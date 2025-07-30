package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.BitCollection
import com.kelvsyc.kotlin.core.Bitwise
import com.kelvsyc.kotlin.core.traits.BitShift
import com.kelvsyc.kotlin.core.traits.BitStore

object ByteBitStore : BitStore<Byte>,
    BitCollection<Byte> by ByteBitCollection,
    BitShift<Byte> by ByteBitShift,
    Bitwise<Byte> by ByteBitwise

object UByteBitStore : BitStore<UByte>,
    BitCollection<UByte> by UByteBitCollection,
    BitShift<UByte> by UByteBitShift,
    Bitwise<UByte> by UByteBitwise

object ShortBitStore : BitStore<Short>,
    BitCollection<Short> by ShortBitCollection,
    BitShift<Short> by ShortBitShift,
    Bitwise<Short> by ShortBitwise

object UShortBitStore : BitStore<UShort>,
    BitCollection<UShort> by UShortBitCollection,
    BitShift<UShort> by UShortBitShift,
    Bitwise<UShort> by UShortBitwise

object IntBitStore : BitStore<Int>,
    BitCollection<Int> by IntBitCollection,
    BitShift<Int> by IntBitShift,
    Bitwise<Int> by IntBitwise

object UIntBitStore : BitStore<UInt>,
    BitCollection<UInt> by UIntBitCollection,
    BitShift<UInt> by UIntBitShift,
    Bitwise<UInt> by UIntBitwise

object LongBitStore : BitStore<Long>,
    BitCollection<Long> by LongBitCollection,
    BitShift<Long> by LongBitShift,
    Bitwise<Long> by LongBitwise

object ULongBitStore : BitStore<ULong>,
    BitCollection<ULong> by ULongBitCollection,
    BitShift<ULong> by ULongBitShift,
    Bitwise<ULong> by ULongBitwise
