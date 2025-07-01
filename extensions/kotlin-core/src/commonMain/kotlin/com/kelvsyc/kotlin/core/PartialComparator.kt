package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.PartialComparatorAdapter

/**
 * Interface representing a partial comparison of two elements of a specific type.
 *
 * `PartialComparator` should compare elements in the same manner as that of a [Comparator], except that two elements
 * can be considered incomparable, for which `null` can be returned.
 */
fun interface PartialComparator<T> {
    companion object {
        fun <T> of(comparator: Comparator<T>): PartialComparator<T> = PartialComparatorAdapter(comparator)
    }

    /**
     * Compares two items, in the same manner as that of a [Comparator].
     *
     * @return A positive value if the first item is greater, a negative value if the second item is greater, `0` if the
     *         two items are equal, and `null` if the two items are incomparable.
     * @see Comparator
     */
    fun compare(lhs: T, rhs: T): Int?
}
