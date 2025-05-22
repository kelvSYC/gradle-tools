@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.BiMap
import com.google.common.collect.EnumBiMap
import com.google.common.collect.EnumHashBiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap

/**
 * Builds a new read-only [BiMap] by populating an [ImmutableBiMap.Builder] using the given [action].
 */
fun <K : Any, V : Any> buildBiMap(action: ImmutableBiMap.Builder<K, V>.() -> Unit): BiMap<K, V> =
    ImmutableBiMap.builder<K, V>().apply(action).build()

/**
 * Returns an empty [BiMap].
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> emptyBiMap(): BiMap<K, V> = ImmutableBiMap.of()

/**
 * Returns an empty [BiMap].
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> biMapOf(): BiMap<K, V> = ImmutableBiMap.of()

/**
 * Returns a new read-only [BiMap] containing a single entry.
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> biMapOf(element: Pair<K, V>) = ImmutableBiMap.of(element.first, element.second)

/**
 * Returns a new read-only [BiMap] containing the given entries.
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> biMapOf(e1: Pair<K, V>, e2: Pair<K, V>) =
    ImmutableBiMap.of(e1.first, e1.second, e2.first, e2.second)

/**
 * Returns a new read-only [BiMap] containing the given entries.
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> biMapOf(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>) =
    ImmutableBiMap.of(e1.first, e1.second, e2.first, e2.second, e3.first, e3.second)

/**
 * Returns a new read-only [BiMap] containing the given entries.
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> biMapOf(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>) =
    ImmutableBiMap.of(e1.first, e1.second, e2.first, e2.second, e3.first, e3.second, e4.first, e4.second)

/**
 * Returns a new read-only [BiMap] containing the given entries.
 *
 * @see ImmutableBiMap.of
 */
fun <K : Any, V : Any> biMapOf(e1: Pair<K, V>, e2: Pair<K, V>, e3: Pair<K, V>, e4: Pair<K, V>, e5: Pair<K, V>) =
    ImmutableBiMap.of(e1.first, e1.second, e2.first, e2.second, e3.first, e3.second, e4.first, e4.second, e5.first, e5.second)

/**
 * Returns a new read-only [BiMap] with the specified contents, given as a list of [Pair]s where the first value is
 * the key and the second is the value.
 */
fun <K: Any, V: Any> biMapOf(vararg elements: Pair<K, V>): BiMap<K, V> = buildBiMap {
    elements.forEach {
        put(it.first, it.second)
    }
}

/**
 * Returns a new empty [EnumBiMap].
 */
inline fun <reified K : Enum<K>, reified V : Enum<V>> enumBiMapOf() = EnumBiMap.create(K::class.java, V::class.java)

/**
 * Returns a new [EnumBiMap] using the specified entries.
 */
fun <K : Enum<K>, V : Enum<V>> enumBiMapOf(vararg elements: Pair<K, V>) = EnumBiMap.create(mapOf(*elements))

/**
 * Returns a new empty [EnumHashBiMap].
 */
inline fun <reified K : Enum<K>, V> enumHashBiMapOf() = EnumHashBiMap.create<K, V>(K::class.java)

/**
 * Returns a new [EnumHashBiMap] using the specified entries.
 */
fun <K : Enum<K>, V> enumHashBiMapOf(vararg elements: Pair<K, V>) = EnumHashBiMap.create(mapOf(*elements))

/**
 * Returns a new empty [HashBiMap].
 */
fun <K, V> hashBiMapOf() = HashBiMap.create<K, V>()

/**
 * Returns a new [HashBiMap] using the specified entries.
 */
fun <K, V> hashBiMapOf(vararg elements: Pair<K, V>) = HashBiMap.create(mapOf(*elements))
