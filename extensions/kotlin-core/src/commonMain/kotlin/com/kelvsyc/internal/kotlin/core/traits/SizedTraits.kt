package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Sized

object ByteSized : Sized<Byte> {
    override val sizeBits: Int get() = Byte.SIZE_BITS
    override val sizeBytes: Int get() = Byte.SIZE_BYTES
}

object UByteSized : Sized<UByte> {
    override val sizeBits: Int get() = UByte.SIZE_BITS
    override val sizeBytes: Int get() = UByte.SIZE_BYTES
}

object ShortSized : Sized<Short> {
    override val sizeBits: Int get() = Short.SIZE_BITS
    override val sizeBytes: Int get() = Short.SIZE_BYTES
}

object UShortSized : Sized<UShort> {
    override val sizeBits: Int get() = UShort.SIZE_BITS
    override val sizeBytes: Int get() = UShort.SIZE_BYTES
}

object IntSized : Sized<Int> {
    override val sizeBits: Int get() = Int.SIZE_BITS
    override val sizeBytes: Int get() = Int.SIZE_BYTES
}

object UIntSized : Sized<UInt> {
    override val sizeBits: Int get() = UInt.SIZE_BITS
    override val sizeBytes: Int get() = UInt.SIZE_BYTES
}

object LongSized : Sized<Long> {
    override val sizeBits: Int get() = Long.SIZE_BITS
    override val sizeBytes: Int get() = Long.SIZE_BYTES
}

object ULongSized : Sized<ULong> {
    override val sizeBits: Int get() = ULong.SIZE_BITS
    override val sizeBytes: Int get() = ULong.SIZE_BYTES
}

object FloatSized : Sized<Float> {
    override val sizeBits: Int get() = Float.SIZE_BITS
    override val sizeBytes: Int get() = Float.SIZE_BYTES
}

object DoubleSized : Sized<Double> {
    override val sizeBits: Int get() = Double.SIZE_BITS
    override val sizeBytes: Int get() = Double.SIZE_BYTES
}
