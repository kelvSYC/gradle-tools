package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits

/**
 * Class defining [BitStoreConstants] for fixed-size [ArrayLike] types with elements that have a well-defined
 * [BitStoreConstants] trait definitions.
 *
 * Generally, instances should be created from the companion object's functions, rather than through the constructor
 * directly, for the sake of convenience.
 *
 * @param arrayTraits Type traits for the array type.
 * @param elementTraits Type traits for the element type.
 * @param A The array type
 * @param E The element type
 */
class ArrayBitStoreConstants<A, E>(
    private val sized: Sized,
    private val arrayTraits: ArrayLike<A, E>,
    private val elementTraits: BitStoreConstants<E>
) : BitStoreConstants<A> {
    companion object {
        inline fun <reified E> ofObjectArray(size: Int, elementSized: Sized, elementTraits: BitStoreConstants<E>): BitStoreConstants<Array<E>> =
            ArrayBitStoreConstants(ArraySized.ofObjectArray<E>(size, elementSized), arrayLike(), elementTraits)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [ByteArray].
         */
        fun ofByteArray(size: Int): BitStoreConstants<ByteArray> =
            ArrayBitStoreConstants(ArraySized.ofByteArray(size), TypeTraits.ByteArray, TypeTraits.Byte)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [UByteArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUByteArray(size: Int): BitStoreConstants<UByteArray> =
            ArrayBitStoreConstants(ArraySized.ofUByteArray(size), TypeTraits.UByteArray, TypeTraits.UByte)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [ShortArray].
         */
        fun ofShortArray(size: Int): BitStoreConstants<ShortArray> =
            ArrayBitStoreConstants(ArraySized.ofShortArray(size), TypeTraits.ShortArray, TypeTraits.Short)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [UShortArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUShortArray(size: Int): BitStoreConstants<UShortArray> =
            ArrayBitStoreConstants(ArraySized.ofUShortArray(size), TypeTraits.UShortArray, TypeTraits.UShort)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [IntArray].
         */
        fun ofIntArray(size: Int): BitStoreConstants<IntArray> =
            ArrayBitStoreConstants(ArraySized.ofIntArray(size), TypeTraits.IntArray, TypeTraits.Int)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [UIntArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUIntArray(size: Int): BitStoreConstants<UIntArray> =
            ArrayBitStoreConstants(ArraySized.ofUIntArray(size), TypeTraits.UIntArray, TypeTraits.UInt)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [LongArray].
         */
        fun ofLongArray(size: Int): BitStoreConstants<LongArray> =
            ArrayBitStoreConstants(ArraySized.ofLongArray(size), TypeTraits.LongArray, TypeTraits.Long)

        /**
         * Returns a [BitStoreConstants] object for a fixed-size [ULongArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofULongArray(size: Int): BitStoreConstants<ULongArray> =
            ArrayBitStoreConstants(ArraySized.ofULongArray(size), TypeTraits.ULongArray, TypeTraits.ULong)
    }

    override val allClear: A by lazy { arrayTraits.create(sized.sizeBits) { elementTraits.allClear } }
    override val allSet: A by lazy { arrayTraits.create(sized.sizeBits) {elementTraits.allSet} }
    override fun hasSetBits(value: A): Boolean = arrayTraits.any(value, elementTraits::hasSetBits)
    override fun isAllClear(value: A): Boolean = arrayTraits.all(value, elementTraits::isAllClear)
}
