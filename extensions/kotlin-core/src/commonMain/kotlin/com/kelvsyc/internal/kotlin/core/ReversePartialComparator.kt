package com.kelvsyc.internal.kotlin.core

import com.kelvsyc.kotlin.core.PartialComparator

class ReversePartialComparator<T>(internal val base: PartialComparator<T>) : PartialComparator<T> {
    override fun compare(lhs: T, rhs: T): Int? = base.compare(rhs, lhs)
}
