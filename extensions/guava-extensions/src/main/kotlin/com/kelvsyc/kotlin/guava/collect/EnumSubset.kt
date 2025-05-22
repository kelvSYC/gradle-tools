package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.Comparators
import java.util.*

/**
 * `EnumSubset` is a wrapper around a [Set] of enum constants that implement [Comparable] via lexicographical ordering
 * of their [ordinal][Enum.ordinal] values.
 *
 * Such an ordering allows it to be used with [DiscreteDomain][com.google.common.collect.DiscreteDomain] via
 * [EnumSubsetDomain].
 *
 * Internally, an [EnumSubset] uses an [EnumSet] as its canonical [Set] implementation; this set is treated as immutable.
 *
 * @param E the enum type
 */
@JvmInline
value class EnumSubset<E : Enum<E>> private constructor(private val value: EnumSet<E>): Set<E> by value, Comparable<EnumSubset<E>> {
    companion object {
        fun <E : Enum<E>> of(elements: EnumSet<E>) = EnumSubset(EnumSet.copyOf(elements))
        fun <E : Enum<E>> of(elements: Iterable<E>) = EnumSubset(EnumSet.copyOf(elements.toList()))
        fun <E : Enum<E>> of(vararg elements: E) = EnumSubset(EnumSet.copyOf(elements.asList()))
    }

    private val ordinals
        get() = value.map(Enum<E>::ordinal)

    override fun compareTo(other: EnumSubset<E>): Int {
        val comparator = Comparators.lexicographical(Comparator.naturalOrder<Int>())
        return comparator.compare(ordinals, other.ordinals)
    }
}
