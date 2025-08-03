@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.SetMultimap

/**
 * Builds a new read-only [Multimap] by populating an [ImmutableMultimap.Builder] using the given [action].
 */
fun <K : Any, V : Any> buildMultimap(action: ImmutableMultimap.Builder<K, V>.() -> Unit): Multimap<K, V> =
    ImmutableMultimap.builder<K, V>().apply(action).build()

/**
 * Returns an empty [Multimap].
 *
 * @see ImmutableMultimap.of
 */
fun <K : Any, V : Any> emptyMultimap(): Multimap<K, V> = ImmutableMultimap.of()

/**
 * Returns an empty [Multimap].
 *
 * @see ImmutableMultimap.of
 */
fun <K : Any, V : Any> multimapOf(): Multimap<K, V> = ImmutableMultimap.of()

/**
 * Returns a new read-only [Multimap] containing a single entry.
 *
 * @see ImmutableMultimap.of
 */
fun <K: Any, V : Any> multimapOf(element: Pair<K, V>): Multimap<K, V> = ImmutableMultimap.of(element.first, element.second)

/**
 * Returns a new read-only [Multimap] with the specified contents, given as a list of [Pair]s where the first value is
 * the key and the second is the value.
 */
fun <K : Any, V : Any> multimapOf(vararg elements: Pair<K, V>): Multimap<K, V> = buildMultimap {
    elements.forEach {
        put(it.first, it.second)
    }
}

/**
 * Builds a new read-only [ListMultimap] by populating an [ImmutableListMultimap.Builder] using the given [action].
 */
fun <K : Any, V : Any> buildListMultimap(action: ImmutableListMultimap.Builder<K, V>.() -> Unit): ListMultimap<K, V> =
    ImmutableListMultimap.builder<K, V>().apply(action).build()

/**
 * Returns an empty [ListMultimap].
 *
 * @see ImmutableListMultimap.of
 */
fun <K : Any, V : Any> emptyListMultimap(): ListMultimap<K, V> = ImmutableListMultimap.of()

/**
 * Returns an empty [ListMultimap].
 *
 * @see ImmutableListMultimap.of
 */
fun <K : Any, V : Any> listMultimapOf(): ListMultimap<K, V> = ImmutableListMultimap.of()

/**
 * Returns a new read-only [ListMultimap] with the specified elements, given as a list of [Pair]s where the first value
 * is the key and the second is the value.
 */
fun <K : Any, V : Any> listMultimapOf(vararg elements: Pair<K, V>): ListMultimap<K, V> = ImmutableListMultimap.builder<K, V>().apply {
    elements.forEach {
        put(it.first, it.second)
    }
}.build()

/**
 * Builds a new read-only [SetMultimap] by populating an [ImmutableSetMultimap.Builder] using the given [action].
 */
fun <K : Any, V : Any> buildSetMultimap(action: ImmutableSetMultimap.Builder<K, V>.() -> Unit): SetMultimap<K, V> =
    ImmutableSetMultimap.builder<K, V>().apply(action).build()


/**
 * Returns an empty [SetMultimap].
 *
 * @see ImmutableSetMultimap.of
 */
fun <K : Any, V : Any> emptySetMultimap(): SetMultimap<K, V> = ImmutableSetMultimap.of()

/**
 * Returns an empty [SetMultimap].
 *
 * @see ImmutableSetMultimap.of
 */
fun <K : Any, V : Any> setMultimapOf(): SetMultimap<K, V> = ImmutableSetMultimap.of()

/**
 * Returns a new read-only [SetMultimap] with the specified contents, given as a list of [Pair]s where the first value
 * is the key and the second is the value.
 */
fun <K : Any, V : Any> setMultimapOf(vararg elements: Pair<K, V>): SetMultimap<K, V> = buildSetMultimap {
    elements.forEach {
        put(it.first, it.second)
    }
}
