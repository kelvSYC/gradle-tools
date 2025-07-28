package com.kelvsyc.kotlin.core.traits

/**
 * Returns a [Sized] implementation for a fixed-size array of an element type with a [Sized] implementation.
 *
 * @param size The size of the array
 * @param base The [Sized] implementation for the element type.
 * @param E The element type
 */
fun <E> arraySized(size: Int, base: Sized<E>): Sized<Array<E>> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<Array<E>> {
        override val sizeBits: Int = size * base.sizeBits
        override val sizeBytes: Int = size * base.sizeBytes
    }
}

fun byteArraySized(size: Int): Sized<ByteArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<ByteArray> {
        override val sizeBits: Int = size * Byte.SIZE_BITS
        override val sizeBytes: Int = size * Byte.SIZE_BYTES
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun uByteArraySized(size: Int): Sized<UByteArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<UByteArray> {
        override val sizeBits: Int = size * UByte.SIZE_BITS
        override val sizeBytes: Int = size * UByte.SIZE_BYTES
    }
}

fun shortArraySized(size: Int): Sized<ShortArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<ShortArray> {
        override val sizeBits: Int = size * Short.SIZE_BITS
        override val sizeBytes: Int = size * Short.SIZE_BYTES
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun uShortArraySized(size: Int): Sized<UShortArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<UShortArray> {
        override val sizeBits: Int = size * Short.SIZE_BITS
        override val sizeBytes: Int = size * Short.SIZE_BYTES
    }
}

fun intArraySized(size: Int): Sized<IntArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<IntArray> {
        override val sizeBits: Int = size * Int.SIZE_BITS
        override val sizeBytes: Int = size * Int.SIZE_BYTES
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun uIntArraySized(size: Int): Sized<UIntArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<UIntArray> {
        override val sizeBits: Int = size * Int.SIZE_BITS
        override val sizeBytes: Int = size * Int.SIZE_BYTES
    }
}

fun longArraySized(size: Int): Sized<LongArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<LongArray> {
        override val sizeBits: Int = size * Long.SIZE_BITS
        override val sizeBytes: Int = size * Long.SIZE_BYTES
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun uLongArraySized(size: Int): Sized<ULongArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<ULongArray> {
        override val sizeBits: Int = size * Long.SIZE_BITS
        override val sizeBytes: Int = size * Long.SIZE_BYTES
    }
}

fun floatArraySized(size: Int): Sized<FloatArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<FloatArray> {
        override val sizeBits: Int = size * Float.SIZE_BITS
        override val sizeBytes: Int = size * Float.SIZE_BYTES
    }
}

fun doubleArraySized(size: Int): Sized<DoubleArray> {
    require(size >= 0) { "Cannot create a Sized object for negative array sizes" }
    return object : Sized<DoubleArray> {
        override val sizeBits: Int = size * Double.SIZE_BITS
        override val sizeBytes: Int = size * Double.SIZE_BYTES
    }
}
