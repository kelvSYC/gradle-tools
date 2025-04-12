package com.kelvsyc.kotlin.guava

import com.google.common.primitives.SignedBytes
import com.google.common.primitives.UnsignedBytes
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLongs

object Comparators {
    /**
     * Returns a [Comparator] comparing two [Byte] instances, treating the values as signed bytes.
     *
     * @see SignedBytes.compare
     */
    val signedByteComparator = Comparator(SignedBytes::compare)

    /**
     * Returns a [Comparator] comparing two [Byte] instances, treating the values as unsigned bytes.
     *
     * @see UnsignedBytes.compare
     */
    val unsignedByteComparator = Comparator(UnsignedBytes::compare)

    /**
     * Returns a [Comparator] comparing two [Int] instances, treating the values as unsigned ints.
     *
     * @see UnsignedInts.compare
     */
    val unsignedIntComparator = Comparator(UnsignedInts::compare)

    /**
     * Returns a [Comparator] comparing two [Long] instances, treating the values as unsigned longs.
     */
    val unsignedLongComparator = Comparator(UnsignedLongs::compare)
}
