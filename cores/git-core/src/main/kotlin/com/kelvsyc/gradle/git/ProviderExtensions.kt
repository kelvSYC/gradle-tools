package com.kelvsyc.gradle.git

import org.gradle.api.provider.Provider
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
