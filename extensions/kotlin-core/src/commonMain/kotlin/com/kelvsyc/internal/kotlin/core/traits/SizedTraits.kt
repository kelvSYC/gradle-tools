package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Sized

interface ByteSized : Sized<Byte> {
    override val sizeBits: Int
        get() = Byte.SIZE_BITS
}

interface UByteSized : Sized<UByte> {
    override val sizeBits: Int
        get() = UByte.SIZE_BITS
}

interface ShortSized : Sized<Short> {
    override val sizeBits: Int
        get() = Short.SIZE_BITS
}

interface UShortSized : Sized<UShort> {
    override val sizeBits: Int
        get() = UShort.SIZE_BITS
}

interface IntSized : Sized<Int> {
    override val sizeBits: Int
        get() = Int.SIZE_BITS
}

interface UIntSized : Sized<UInt> {
    override val sizeBits: Int
        get() = UInt.SIZE_BITS
}

interface LongSized : Sized<Long> {
    override val sizeBits: Int
        get() = Long.SIZE_BITS
}

interface ULongSized : Sized<ULong> {
    override val sizeBits: Int
        get() = ULong.SIZE_BITS
}
