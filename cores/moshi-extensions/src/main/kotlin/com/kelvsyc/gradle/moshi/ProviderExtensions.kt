package com.kelvsyc.gradle.moshi

import com.kelvsyc.kotlin.moshi.JsonValue
import com.kelvsyc.kotlin.moshi.parseJson
import org.gradle.api.provider.Provider

/**
 * Returns a [Provider] that lazily parses this string provider's value as a [JsonValue] tree.
 */
fun Provider<String>.parseJson(): Provider<JsonValue> = map { it.parseJson() }
