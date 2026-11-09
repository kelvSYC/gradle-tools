@file:JvmName("ProviderFactoryExtensionsKt")
@file:JvmMultifileClass

package com.kelvsyc.gradle.providers

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

/**
 * Returns a [Provider] that provides a constant value.
 *
 * The returned [Provider] is absent if `null` is supplied.
 */
@Deprecated("Use Gradle's ProviderFactory.presentIfNotNull().", ReplaceWith("presentIfNotNull(value)"))
fun <T : Any> ProviderFactory.ofNullable(value: T?): Provider<T> = provider { value }

/**
 * Returns a [Provider] whose value is always absent.
 */
@Deprecated("Use Gradle's ProviderFactory.absent().", ReplaceWith("absent()"))
val ProviderFactory.absent: Provider<Nothing>
    get() = provider { null }
