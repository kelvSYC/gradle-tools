package com.kelvsyc.gradle.providers

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

/**
 * Returns a [Provider] that provides a constant value.
 *
 * The returned [Provider] is absent if `null` is supplied.
 */
fun <T> ProviderFactory.ofNullable(value: T?): Provider<T> = provider { value }

/**
 * Returns a [Provider] whose value is always absent.
 */
val ProviderFactory.absent: Provider<Nothing>
    get() = provider { null }

