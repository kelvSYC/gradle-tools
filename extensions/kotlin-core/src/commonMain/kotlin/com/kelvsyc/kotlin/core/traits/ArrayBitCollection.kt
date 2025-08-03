package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.internal.kotlin.core.traits.ByteArrayBitCollection
import com.kelvsyc.internal.kotlin.core.traits.UByteArrayBitCollection
import com.kelvsyc.kotlin.core.traits.BitCollection
import com.kelvsyc.kotlin.core.TypeTraits
import kotlin.math.max
import kotlin.math.min

/**
 * Class defining [BitCollection] for fixed-size [ArrayLike] types with elements that have a well-defined
 * [BitCollection] trait definition.
 *
 * Generally, instances should be created from the companion object's functions, rather than through the constructor
 * directly, for the sake of convenience.
 *
 * @param arrayTraits Type traits for the array type.
 * @param elementTraits Type traits for the element type.
 * @param A The array type
 * @param E The element type
 */
open class ArrayBitCollection<A, E>(
    private val sized: ArraySized<A, E>,
    private val arrayTraits: ArrayLike<A, E>,
    private val elementTraits: BitCollection<E>
) : BitCollection<A> {
    companion object {
        /**
         * Returns a [BitCollection] object for a fixed-size object array of element type for which a well-defined
         * [BitCollection] definition exists.
         */
        inline fun <reified E> ofObjectArray(size: Int, elementSized: Sized<E>, elementTraits: BitCollection<E>): BitCollection<Array<E>> =
            ArrayBitCollection(ArraySized.ofObjectArray(size, elementSized), arrayLike(), elementTraits)

        /**
         * Returns a [BitCollection] object for a fixed-size [ByteArray].
         */
        fun ofByteArray(size: Int): BitCollection<ByteArray> = ByteArrayBitCollection(ArraySized.ofByteArray(size))

        /**
         * Returns a [BitCollection] object for a fixed-size [UByteArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUByteArray(size: Int): BitCollection<UByteArray> = UByteArrayBitCollection(ArraySized.ofUByteArray(size))

        /**
         * Returns a [BitCollection] object for a fixed-size [ShortArray].
         */
        fun ofShortArray(size: Int): BitCollection<ShortArray> =
            ArrayBitCollection(ArraySized.ofShortArray(size), TypeTraits.ShortArray, TypeTraits.Short)

        /**
         * Returns a [BitCollection] object for a fixed-size [UShortArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUShortArray(size: Int): BitCollection<UShortArray> =
            ArrayBitCollection(ArraySized.ofUShortArray(size), TypeTraits.UShortArray, TypeTraits.UShort)

        /**
         * Returns a [BitCollection] object for a fixed-size [IntArray].
         */
        fun ofIntArray(size: Int): BitCollection<IntArray> =
            ArrayBitCollection(ArraySized.ofIntArray(size), TypeTraits.IntArray, TypeTraits.Int)

        /**
         * Returns a [BitCollection] object for a fixed-size [UIntArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofUIntArray(size: Int): BitCollection<UIntArray> =
            ArrayBitCollection(ArraySized.ofUIntArray(size), TypeTraits.UIntArray, TypeTraits.UInt)

        /**
         * Returns a [BitCollection] object for a fixed-size [LongArray].
         */
        fun ofLongArray(size: Int): BitCollection<LongArray> =
            ArrayBitCollection(ArraySized.ofLongArray(size), TypeTraits.LongArray, TypeTraits.Long)

        /**
         * Returns a [BitCollection] object for a fixed-size [ULongArray].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun ofULongArray(size: Int): BitCollection<ULongArray> =
            ArrayBitCollection(ArraySized.ofULongArray(size), TypeTraits.ULongArray, TypeTraits.ULong)
    }

    override fun fromBits(bits: IntRange): A {
        require(bits.start >= 0 && bits.endInclusive < sized.sizeBits) { "Bit count out of range" }

        return arrayTraits.create(sized.arraySize) {
            val startIndex = it * sized.elementSized.sizeBits
            val endIndex = startIndex + sized.elementSized.sizeBits - 1
            val indexRange = startIndex .. endIndex
            val lower = max(bits.start, startIndex)
            val upper = min(bits.endInclusive, endIndex)
            if (indexRange.contains(lower) && indexRange.contains(upper)) {
                val intersection =  lower.rem(sized.elementSized.sizeBits) .. upper.rem(sized.elementSized.sizeBits)
                elementTraits.fromBits(intersection)
            } else {
                // The intersection range is definitely empty
                elementTraits.fromBits(IntRange.EMPTY) // Simulated zero
            }
        }
    }

    override fun asBitSequence(value: A): Sequence<Boolean> =
        arrayTraits.map(value, elementTraits::asBitSequence).asSequence().flatten()

    override fun asByteArray(value: A): ByteArray {
        val sizeBytes = sized.elementSized.sizeBytes
        val result = ByteArray(sized.sizeBytes)
        arrayTraits.map(value, elementTraits::asByteArray).forEachIndexed { index, bytes ->
            // We trust that bytes.size == sizeBytes, but just in case...
            bytes.copyInto(result, index * sizeBytes, 0, sizeBytes)
        }
        return result
    }

    override fun getSetBits(value: A): Set<Int> = buildSet {
        arrayTraits.forEachIndexed(value) { index, e ->
            addAll(elementTraits.getSetBits(e).map { index * sized.elementSized.sizeBits + it })
        }
    }

    override fun countLeadingZeroBits(value: A): Int {
        val idx = arrayTraits.indexOfLast(value) { elementTraits.countLeadingZeroBits(it) != sized.elementSized.sizeBits }
        return if (idx == -1) {
            // The array is all zeroes
            sized.sizeBits
        } else {
            (arrayTraits.getSize(value) - 1 - idx) * sized.elementSized.sizeBits + elementTraits.countLeadingZeroBits(arrayTraits.getAt(value, idx))
        }
    }

    override fun countTrailingZeroBits(value: A): Int {
        val idx = arrayTraits.indexOfFirst(value) { elementTraits.countTrailingZeroBits(it) != sized.elementSized.sizeBits }
        return if (idx == -1) {
            // The array is all zeroes
            sized.sizeBits
        } else {
            idx * sized.elementSized.sizeBits + elementTraits.countTrailingZeroBits(arrayTraits.getAt(value, idx))
        }
    }
}
