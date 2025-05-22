package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableSortedMultiset
import com.google.common.collect.SortedMultiset

/**
 * Builds a new read-only [SortedMultiset], with elements ordered by their natural ordering, by populating an
 * [ImmutableSortedMultiset.Builder] using the given [action].
 */
fun <E : Comparable<E>> buildSortedMultiset(action: ImmutableSortedMultiset.Builder<E>.() -> Unit): SortedMultiset<E> =
    ImmutableSortedMultiset.naturalOrder<E>().apply(action).build()

/**
 * Builds a new read-only [SortedMultiset], with elements ordered by the specified [Comparator], by populating an
 * [ImmutableSortedMultiset.Builder] using the given [action].
 */
fun <E : Any> buildSortedMultiset(comparator: Comparator<in E>, action: ImmutableSortedMultiset.Builder<E>.() -> Unit): SortedMultiset<E> =
    ImmutableSortedMultiset.Builder(comparator).apply(action).build()

/**
 * Returns an empty [SortedMultiset].
 *
 * @see ImmutableSortedMultiset.of
 */
fun <E : Any> emptySortedMultiset(): SortedMultiset<E> = ImmutableSortedMultiset.of()

/**
 * Returns an empty [SortedMultiset].
 *
 * @see ImmutableSortedMultiset.of
 */
fun <E : Any> sortedMultisetOf(): SortedMultiset<E> = ImmutableSortedMultiset.of()

/**
 * Returns a new read-only [SortedMultiset], with elements ordered by their natural ordering, containing the given entries.
 *
 * @see ImmutableSortedMultiset.copyOf
 */
fun <E : Comparable<E>> sortedMultisetOf(vararg elements: E): SortedMultiset<E> =
    ImmutableSortedMultiset.copyOf(naturalOrder(), elements.asIterable())

/**
 * Returns a new read-only [SortedMultiset], with elements ordered by the specified [Comparator], containing the given entries.
 *
 * @see ImmutableSortedMultiset.copyOf
 */
fun <E : Any> sortedMultisetOf(comparator: Comparator<in E>, vararg elements: E): SortedMultiset<E> =
    ImmutableSortedMultiset.copyOf(comparator, elements.asIterable())
