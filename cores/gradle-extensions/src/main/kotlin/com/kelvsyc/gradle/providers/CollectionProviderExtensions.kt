package com.kelvsyc.gradle.providers

import org.gradle.api.provider.Provider
import java.util.*

/**
 * Returns a provider resolving to the value of the specified key.
 *
 * The returned provider has no value if this provider has no value, or if the mapping has no value for this key.
 *
 * Syntactic shorthand for [mapKt]` { it[key] }`.
 *
 * This function extends the semantics of [MapProperty.getting()][org.gradle.api.provider.MapProperty.getting] to
 * generic providers of maps, and thus will never be used on objects with a declared type of
 * [MapProperty][org.gradle.api.provider.MapProperty].
 */
fun <K : Any, V : Any> Provider<Map<K, V>>.getting(key: K) = mapKt { it[key] }

/**
 * Returns a provider resolving to the value of the specified key.
 *
 * The returned provider has no value if the key provider has no value, if this provider has no value, or if the mapping
 * has no value for this key.
 */
fun <K : Any, V : Any> Provider<Map<K, V>>.getting(key: Provider<K>) = zip(key, Map<K, V>::get)

/**
 * Maps the elements of the collection provided by this provider.
 *
 * Syntactic shorthand for `map { it.map(fn) }`
 */
fun <T, R> Provider<out Iterable<T>>.mapElements(fn: (T) -> R) = map { it.map(fn) }

/**
 * Maps the elements of the collection provided by this provider.
 *
 * Syntactic shorthand for `map { it.mapNotNull(fn) }`
 */
fun <T, R : Any> Provider<out Iterable<T>>.mapElementsNotNull(fn: (T) -> R?) = map { it.mapNotNull(fn) }

/**
 * Returns a [Provider] that returns an empty list if this provider is absent.
 *
 * Syntactic shorthand for `orElse(emptyList())`
 */
val <T> Provider<List<T>>.orElseEmpty
    @JvmName("listProviderOrElseEmpty")
    get() = orElse(emptyList())

/**
 * Returns a [Provider] that returns an empty set if this provider is absent.
 *
 * Syntactic shorthand for `orElse(emptySet())`
 */
val <T> Provider<Set<T>>.orElseEmpty
    @JvmName("setProviderOrElseEmpty")
    get() = orElse(emptySet())

/**
 * Returns a [Provider] that returns an empty map if this provider is absent.
 *
 * Syntactic shorthand for `orElse(emptyMap())`
 */
val <K, V> Provider<Map<K, V>>.orElseEmpty
    @JvmName("mapProviderOrElseEmpty")
    get() = orElse(emptyMap())

/**
 * Returns a [Provider] converting the provided [Properties] object into a string map.
 */
val Provider<Properties>.asMap
    get() = map {
        it.stringPropertyNames().associateWith(it::getProperty)
    }
