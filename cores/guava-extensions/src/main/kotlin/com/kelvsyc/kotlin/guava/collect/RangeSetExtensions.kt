package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import com.google.common.collect.TreeRangeSet

/**
 * Builds a new read-only [RangeSet] by populating an [ImmutableRangeSet.Builder] using the given [action].
 */
fun <C : Comparable<C>> buildRangeSet(action: ImmutableRangeSet.Builder<C>.() -> Unit): RangeSet<C> =
    ImmutableRangeSet.builder<C>().apply(action).build()

/**
 * Returns an empty [RangeSet].
 *
 * @see ImmutableRangeSet.of
 */
fun <C : Comparable<C>> emptyRangeSet(): RangeSet<C> = ImmutableRangeSet.of<C>()

/**
 * Returns an empty [RangeSet].
 *
 * @see ImmutableRangeSet.of
 */
fun <C : Comparable<C>> rangeSetOf(): RangeSet<C> = ImmutableRangeSet.of<C>()

/**
 * Returns a new read-only [RangeSet] containing the specified element.
 *
 * @see ImmutableRangeSet.of
 */
fun <C : Comparable<C>> rangeSetOf(element: Range<C>) = ImmutableRangeSet.of<C>(element)

/**
 * Returns a new read-only [RangeSet] containing the union of the given ranges.
 *
 * @see ImmutableRangeSet.of
 */
fun <C : Comparable<C>> rangeSetOf(vararg elements: Range<C>) = ImmutableRangeSet.unionOf<C>(elements.asIterable())

/**
 * Returns a new empty [TreeRangeSet].
 *
 * @see TreeRangeSet.create
 */
fun <C : Comparable<C>> treeRangeSetOf(): TreeRangeSet<C> = TreeRangeSet.create<C>()

/**
 * Returns a new [TreeRangeSet] containing the union of the given ranges.
 *
 * @see TreeRangeSet.create
 */
fun <C : Comparable<C>> treeRangeSetOf(vararg elements: Range<C>) = TreeRangeSet.create(elements.asIterable())
