package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableRangeMap
import com.google.common.collect.Range
import com.google.common.collect.RangeMap
import com.google.common.collect.TreeRangeMap

/**
 * Builds a new read-only [RangeMap] by populating an [ImmutableRangeMap.Builder] using the given [action].
 */
fun <K : Comparable<K>, V : Any> buildRangeMap(action: ImmutableRangeMap.Builder<K, V>.() -> Unit): RangeMap<K, V> =
    ImmutableRangeMap.builder<K, V>().apply(action).build()

/**
 * Returns an empty [RangeMap].
 *
 * @see ImmutableRangeMap.of
 */
fun <K : Comparable<K>, V : Any> emptyRangeMap(): RangeMap<K, V> = ImmutableRangeMap.of<K, V>()

/**
 * Returns an empty [RangeMap].
 *
 * @see ImmutableRangeMap.of
 */
fun <K : Comparable<K>, V : Any> rangeMapOf(): RangeMap<K, V> = ImmutableRangeMap.of<K, V>()

/**
 * Returns a new read-only [RangeMap] containing the specified element.
 *
 * @see ImmutableRangeMap.of
 */
fun <K : Comparable<K>, V : Any> rangeMapOf(element: Pair<Range<K>, V>): RangeMap<K, V> = ImmutableRangeMap.of(element.first, element.second)

/**
 * Returns a new read-only [RangeMap] containing the specified elements.
 *
 * @see ImmutableRangeMap.of
 */
fun <K : Comparable<K>, V : Any> rangeMapOf(vararg elements: Pair<Range<K>, V>): RangeMap<K, V> = buildRangeMap {
    elements.forEach {
        put(it.first, it.second)
    }
}

/**
 * Returns an empty [TreeRangeMap].
 *
 * @see TreeRangeMap.of
 */
fun <K : Comparable<K>, V : Any> treeRangeMapOf(): TreeRangeMap<K, V> = TreeRangeMap.create<K, V>()

/**
 * Returns a new [TreeRangeMap] containing the specified elements.
 *
 * @see TreeRangeMap.of
 */
fun <K : Comparable<K>, V : Any> treeRangeMapOf(vararg elements: Pair<Range<K>, V>) = treeRangeMapOf<K, V>().apply {
    elements.forEach {
        put(it.first, it.second)
    }
}
