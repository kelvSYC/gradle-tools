package com.kelvsyc.gradle.providers

import org.gradle.api.provider.Provider
import java.util.*

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
