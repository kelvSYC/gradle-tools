package com.kelvsyc.gradle.moshi.groovy

import com.kelvsyc.kotlin.moshi.JsonArray
import com.kelvsyc.kotlin.moshi.JsonObject
import com.kelvsyc.kotlin.moshi.JsonValue

/**
 * Navigates into this [JsonValue] by object key, returning `null` if this value is not a
 * [JsonObject] or the key is absent.
 *
 * This extension is a **migration aid** for codebases transitioning from Groovy's `JsonSlurper`,
 * where dynamic property access like `json.foo.bar` is idiomatic. In Kotlin, prefer the typed
 * accessors ([JsonValue.stringAt], [JsonValue.objectAt], etc.) or [JsonValue.at] for path-based
 * navigation once the migration is complete.
 *
 * Remove the import of this package when the migration is finished.
 */
operator fun JsonValue.get(key: String): JsonValue? = when (this) {
    is JsonObject -> this[key]
    else -> null
}

/**
 * Navigates into this [JsonValue] by array index, returning `null` if this value is not a
 * [JsonArray] or the index is out of bounds.
 *
 * This extension is a **migration aid** for codebases transitioning from Groovy's `JsonSlurper`,
 * where indexed access like `json.items[0]` works on any parsed value. In Kotlin, prefer
 * [JsonValue.arrayAt] or [JsonValue.at] for path-based navigation once the migration is complete.
 *
 * Remove the import of this package when the migration is finished.
 */
operator fun JsonValue.get(index: Int): JsonValue? = when (this) {
    is JsonArray -> elements.getOrNull(index)
    else -> null
}
