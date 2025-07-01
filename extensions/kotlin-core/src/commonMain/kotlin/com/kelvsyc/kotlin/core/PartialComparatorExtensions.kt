package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.PartialComparatorAdapter
import com.kelvsyc.internal.kotlin.core.ReversePartialComparator

/**
 * Returns a [PartialComparator] that imposes a reverse partial order using this partial comparator.
 */
fun <T> PartialComparator<T>.reversed() = when (this) {
    is ReversePartialComparator -> base
    is PartialComparatorAdapter -> PartialComparatorAdapter(base.reversed())
    else -> ReversePartialComparator(this)
}
