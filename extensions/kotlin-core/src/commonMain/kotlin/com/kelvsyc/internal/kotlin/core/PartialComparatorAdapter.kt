package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.PartialComparator

class PartialComparatorAdapter<T>(internal val base: Comparator<T>): PartialComparator<T> {
    override fun compare(lhs: T, rhs: T): Int? = base.compare(lhs, rhs)
}
