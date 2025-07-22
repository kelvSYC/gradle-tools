package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.BitCollection
import com.kelvsyc.kotlin.core.BitShift
import com.kelvsyc.kotlin.core.Bitwise
import com.kelvsyc.kotlin.core.traits.BitStore

object ByteBitStore : BitStore<Byte>,
    BitCollection<Byte> by ByteBitCollection,
    BitShift<Byte> by ByteBitShift,
    Bitwise<Byte> by ByteBitwise {
    override val sizeBits: Int by Byte.Companion::SIZE_BITS
}

object UByteBitStore : BitStore<UByte>,
    BitCollection<UByte> by UByteBitCollection,
    BitShift<UByte> by UByteBitShift,
    Bitwise<UByte> by UByteBitwise {
    override val sizeBits: Int by UByte.Companion::SIZE_BITS
}

object ShortBitStore : BitStore<Short>,
    BitCollection<Short> by ShortBitCollection,
    BitShift<Short> by ShortBitShift,
    Bitwise<Short> by ShortBitwise {
    override val sizeBits: Int by Short.Companion::SIZE_BITS
}

object UShortBitStore : BitStore<UShort>,
    BitCollection<UShort> by UShortBitCollection,
    BitShift<UShort> by UShortBitShift,
    Bitwise<UShort> by UShortBitwise {
    override val sizeBits: Int by UShort.Companion::SIZE_BITS
}

object IntBitStore : BitStore<Int>,
    BitCollection<Int> by IntBitCollection,
    BitShift<Int> by IntBitShift,
    Bitwise<Int> by IntBitwise {
    override val sizeBits: Int by Int.Companion::SIZE_BITS
}

object UIntBitStore : BitStore<UInt>,
    BitCollection<UInt> by UIntBitCollection,
    BitShift<UInt> by UIntBitShift,
    Bitwise<UInt> by UIntBitwise {
    override val sizeBits: Int by UInt.Companion::SIZE_BITS
}

object LongBitStore : BitStore<Long>,
    BitCollection<Long> by LongBitCollection,
    BitShift<Long> by LongBitShift,
    Bitwise<Long> by LongBitwise {
    override val sizeBits: Int by Long.Companion::SIZE_BITS
}

object ULongBitStore : BitStore<ULong>,
    BitCollection<ULong> by ULongBitCollection,
    BitShift<ULong> by ULongBitShift,
    Bitwise<ULong> by ULongBitwise {
    override val sizeBits: Int by ULong.Companion::SIZE_BITS
}
