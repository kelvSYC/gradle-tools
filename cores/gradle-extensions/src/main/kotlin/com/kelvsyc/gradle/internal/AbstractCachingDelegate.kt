package com.kelvsyc.gradle.internal

import com.github.benmanes.caffeine.cache.Caffeine
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Simple Kotlin property delegate implementation, backed by a
 * [LoadingCache][com.github.benmanes.caffeine.cache.LoadingCache].
 *
 * @param fn    Function transforming the owning reference to the desired value, used to populate the cache
 */
abstract class AbstractCachingDelegate<T : Any, V>(fn: (T) -> V) : ReadOnlyProperty<T, V> {
    private val cache = Caffeine.newBuilder().apply {
        weakKeys()
    }.build(fn)

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return cache.get(thisRef)
    }
}
