package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.ByteSized
import com.kelvsyc.internal.kotlin.core.traits.DoubleSized
import com.kelvsyc.internal.kotlin.core.traits.FloatSized
import com.kelvsyc.internal.kotlin.core.traits.IntSized
import com.kelvsyc.internal.kotlin.core.traits.LongSized
import com.kelvsyc.internal.kotlin.core.traits.ObjectArraySized
import com.kelvsyc.internal.kotlin.core.traits.ShortSized
import com.kelvsyc.internal.kotlin.core.traits.UByteSized
import com.kelvsyc.internal.kotlin.core.traits.UIntSized
import com.kelvsyc.internal.kotlin.core.traits.ULongSized
import com.kelvsyc.internal.kotlin.core.traits.UShortSized

/**
 * [Sized] implementation for fixed-sized arrays of a particular [Sized] type.
 *
 * @param A The array type
 * @param E The element type
 */
interface ArraySized<A, E> : Sized<A> {
    @Suppress("detekt:TooManyFunctions")
    companion object {
        /**
         * Returns an [ArraySized] object for a fixed-size [ByteArray].
         */
        fun ofByteArray(size: Int) : ArraySized<ByteArray, Byte> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<ByteArray, Byte> {
                override val arraySize: Int = size
                override val elementSized: Sized<Byte> = object : ByteSized {}
                override val sizeBits: Int = size * Byte.SIZE_BITS
                override val sizeBytes: Int = size * Byte.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [UByteArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUByteArray(size: Int) : ArraySized<UByteArray, UByte> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<UByteArray, UByte> {
                override val arraySize: Int = size
                override val elementSized: Sized<UByte> = object : UByteSized {}
                override val sizeBits: Int = size * UByte.SIZE_BITS
                override val sizeBytes: Int = size * UByte.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [ShortArray].
         */
        fun ofShortArray(size: Int) : ArraySized<ShortArray, Short> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<ShortArray, Short> {
                override val arraySize: Int = size
                override val elementSized: Sized<Short> = object : ShortSized {}
                override val sizeBits: Int = size * Short.SIZE_BITS
                override val sizeBytes: Int = size * Short.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [UShortArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUShortArray(size: Int) : ArraySized<UShortArray, UShort> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<UShortArray, UShort> {
                override val arraySize: Int = size
                override val elementSized: Sized<UShort> = object : UShortSized {}
                override val sizeBits: Int = size * UShort.SIZE_BITS
                override val sizeBytes: Int = size * UShort.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [IntArray].
         */
        fun ofIntArray(size: Int) : ArraySized<IntArray, Int> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<IntArray, Int> {
                override val arraySize: Int = size
                override val elementSized: Sized<Int> = object : IntSized {}
                override val sizeBits: Int = size * Int.SIZE_BITS
                override val sizeBytes: Int = size * Int.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [UIntArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUIntArray(size: Int) : ArraySized<UIntArray, UInt> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<UIntArray, UInt> {
                override val arraySize: Int = size
                override val elementSized: Sized<UInt> = object : UIntSized {}
                override val sizeBits: Int = size * UInt.SIZE_BITS
                override val sizeBytes: Int = size * UInt.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [LongArray].
         */
        fun ofLongArray(size: Int) : ArraySized<LongArray, Long> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<LongArray, Long> {
                override val arraySize: Int = size
                override val elementSized: Sized<Long> = object : LongSized {}
                override val sizeBits: Int = size * Long.SIZE_BITS
                override val sizeBytes: Int = size * Long.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [ULongArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofULongArray(size: Int) : ArraySized<ULongArray, ULong> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<ULongArray, ULong> {
                override val arraySize: Int = size
                override val elementSized: Sized<ULong> = object : ULongSized {}
                override val sizeBits: Int = size * ULong.SIZE_BITS
                override val sizeBytes: Int = size * ULong.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [FloatArray].
         */
        fun ofFloatArray(size: Int) : ArraySized<FloatArray, Float> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<FloatArray, Float> {
                override val arraySize: Int = size
                override val elementSized: Sized<Float> = object : FloatSized {}
                override val sizeBits: Int = size * Float.SIZE_BITS
                override val sizeBytes: Int = size * Float.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] object for a fixed-size [DoubleArray].
         */
        fun ofDoubleArray(size: Int) : ArraySized<DoubleArray, Double> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return object : ArraySized<DoubleArray, Double> {
                override val arraySize: Int = size
                override val elementSized: Sized<Double> = object : DoubleSized{}
                override val sizeBits: Int = size * Double.SIZE_BITS
                override val sizeBytes: Int = size * Double.SIZE_BYTES
            }
        }

        /**
         * Returns an [ArraySized] implementation for a fixed-size array of an element type with a [Sized]
         * implementation.
         *
         * @param size The size of the array
         * @param base The [Sized] implementation for the element type.
         * @param E The element type
         */
        fun <E> ofObjectArray(size: Int, base: Sized<E>) : ArraySized<Array<E>, E> {
            require(size >= 0) { "Cannot create an ArraySized object for negative array sizes" }
            return ObjectArraySized(size, base)
        }
    }

    val arraySize: Int
    val elementSized: Sized<E>

    override val sizeBits: Int
        get() = arraySize * elementSized.sizeBits
    override val sizeBytes: Int
        get() = arraySize * elementSized.sizeBytes
}
