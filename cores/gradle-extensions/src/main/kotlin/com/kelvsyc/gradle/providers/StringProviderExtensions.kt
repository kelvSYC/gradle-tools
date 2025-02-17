package com.kelvsyc.gradle.providers

import org.gradle.api.provider.Provider

/**
 * Returns a [Provider] that has no value if this value is blank.
 *
 * Syntactic shorthand for [filter][filter]`(`[String::isNotBlank][String.isNotBlank]`)`
 */
@Suppress("UnstableApiUsage")
fun Provider<String>.filterNotBlank() = filter(String::isNotBlank)

/**
 * Returns a [Provider] that returns an empty string if this provider has no value.
 *
 * Syntactic shorthand for [orElse][Provider.orElse]`("")`
 */
val Provider<String>.orElseEmpty
    get() = orElse("")

/**
 * Returns a [Provider] that converts the provided string to an [Int] value.
 *
 * @see [String.toIntOrNull]
 */
val Provider<String>.asInt
    get() = mapKt(String::toIntOrNull)

/**
 * Returns a [Provider] that converts the provided string to a [Boolean] value.
 *
 * @see [String.toBooleanStrictOrNull]
 */
val Provider<String>.asBoolean
    get() = mapKt(String::toBooleanStrictOrNull)
