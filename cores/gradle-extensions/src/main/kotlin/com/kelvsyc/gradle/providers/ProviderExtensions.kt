package com.kelvsyc.gradle.providers

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import java.util.*

/**
 * Alternative to [Provider.map] that can be used in Kotlin-based Gradle development.
 *
 * See [Gradle issue 12388](https://github.com/gradle/gradle/issues/12388).
 *
 * @see [Provider.map]
 */
@Suppress("UnstableApiUsage")
fun <T : Any, R : Any> Provider<out T>.mapKt(fn: (T) -> R?): Provider<R> =
    map { Optional.ofNullable(fn(it)) }.
    filter(Optional<R>::isPresent).
    map(Optional<R>::get)

/**
 * Alternative to [Provider.flatMap] that can be used in Kotlin-based Gradle development.
 *
 * See [Gradle issue 12388](https://github.com/gradle/gradle/issues/12388).
 *
 * @see [Provider.flatMap]
 */
@Suppress("UnstableApiUsage")
fun <T : Any, R : Any> Provider<out T>.flatMapKt(fn: (T) -> Provider<R>?) =
    map { Optional.ofNullable(fn(it)) }.
    filter(Optional<Provider<R>>::isPresent).
    flatMap(Optional<Provider<R>>::get)

/**
 * Returns a [Provider] that will cache the value of this [Provider]'s [get()][Provider.get] call.
 *
 * This function is implemented by storing the value in new [Property][org.gradle.api.provider.Property].
 *
 * @param objects   [ObjectFactory] instance that can create the backing property.
 */
@JvmName("providerCached")
inline fun <reified T : Any> Provider<T>.cached(objects: ObjectFactory): Provider<T> = objects.property<T>().apply {
    set(this@cached)
    disallowChanges()
    finalizeValueOnRead()
}

/**
 * Returns a [Provider] that will cache the value of this [Provider]'s [get()][Provider.get] call.
 *
 * This function is implemented by storing the value in new [ListProperty][org.gradle.api.provider.ListProperty].
 *
 * @param objects   [ObjectFactory] instance that can create the backing property.
 */
@JvmName("listProviderCached")
inline fun <reified T> Provider<List<T>>.cached(objects: ObjectFactory): Provider<List<T>> =
    objects.listProperty<T>().apply {
        set(this@cached)
        disallowChanges()
        finalizeValueOnRead()
    }

/**
 * Returns a [Provider] that will cache the value of this [Provider]'s [get()][Provider.get] call.
 *
 * This function is implemented by storing the value in new [SetProperty][org.gradle.api.provider.SetProperty].
 *
 * @param objects   [ObjectFactory] instance that can create the backing property.
 */
@JvmName("setProviderCached")
inline fun <reified T> Provider<Set<T>>.cached(objects: ObjectFactory): Provider<Set<T>> =
    objects.setProperty<T>().apply {
        set(this@cached)
        disallowChanges()
        finalizeValueOnRead()
    }

/**
 * Returns a [Provider] that will cache the value of this [Provider]'s [get()][Provider.get] call.
 *
 * This function is implemented by storing the value in new [MapProperty][org.gradle.api.provider.MapProperty].
 *
 * @param objects   [ObjectFactory] instance that can create the backing property.
 */
@JvmName("mapProviderCached")
inline fun <reified K, reified V> Provider<Map<K, V>>.cached(objects: ObjectFactory): Provider<Map<K, V>> =
    objects.mapProperty<K, V>().apply {
        set(this@cached)
        disallowChanges()
        finalizeValueOnRead()
    }
