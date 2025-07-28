package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Sized

interface ByteSized : Sized<Byte> {
    override val sizeBits: Int
        get() = Byte.SIZE_BITS
    override val sizeBytes: Int
        get() = Byte.SIZE_BYTES
}

interface UByteSized : Sized<UByte> {
    override val sizeBits: Int
        get() = UByte.SIZE_BITS
    override val sizeBytes: Int
        get() = UByte.SIZE_BYTES
}

interface ShortSized : Sized<Short> {
    override val sizeBits: Int
        get() = Short.SIZE_BITS
    override val sizeBytes: Int
        get() = Short.SIZE_BYTES
}

interface UShortSized : Sized<UShort> {
    override val sizeBits: Int
        get() = UShort.SIZE_BITS
    override val sizeBytes: Int
        get() = UShort.SIZE_BYTES
}

interface IntSized : Sized<Int> {
    override val sizeBits: Int
        get() = Int.SIZE_BITS
    override val sizeBytes: Int
        get() = Int.SIZE_BYTES
}

interface UIntSized : Sized<UInt> {
    override val sizeBits: Int
        get() = UInt.SIZE_BITS
    override val sizeBytes: Int
        get() = UInt.SIZE_BYTES
}

interface LongSized : Sized<Long> {
    override val sizeBits: Int
        get() = Long.SIZE_BITS
    override val sizeBytes: Int
        get() = Long.SIZE_BYTES
}

interface ULongSized : Sized<ULong> {
    override val sizeBits: Int
        get() = ULong.SIZE_BITS
    override val sizeBytes: Int
        get() = ULong.SIZE_BYTES
}

interface FloatSized : Sized<Float> {
    override val sizeBits: Int
        get() = Float.SIZE_BITS
    override val sizeBytes: Int
        get() = Float.SIZE_BYTES
}

interface DoubleSized : Sized<Double> {
    override val sizeBits: Int
        get() = Double.SIZE_BITS
    override val sizeBytes: Int
        get() = Double.SIZE_BYTES
}
