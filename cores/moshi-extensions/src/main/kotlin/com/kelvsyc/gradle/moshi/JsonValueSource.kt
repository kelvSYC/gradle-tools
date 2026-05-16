package com.kelvsyc.gradle.moshi

import com.kelvsyc.kotlin.moshi.JsonValue
import com.kelvsyc.kotlin.moshi.parseJson
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException

/**
 * Gradle [ValueSource] that provides a [JsonValue] tree parsed from a JSON file.
 *
 * If the input file is not found or cannot be parsed, no value will be provided.
 *
 * **Configuration cache and sensitive files:** Gradle serializes the entire [JsonValue] result to the configuration
 * cache in plaintext when the cache is written. If the JSON file contains sensitive values — API keys, tokens,
 * passwords — those values will be stored in `.gradle/configuration-cache/`. This applies regardless of how the
 * resulting [org.gradle.api.provider.Provider] is stored: a task `@Input`, `@get:Internal`, or private `val` all
 * cause `obtain()` to run at configuration time and the parsed tree to be cached. For files containing sensitive
 * data, read and use the file entirely within a `@TaskAction` or [org.gradle.workers.WorkAction.execute] body
 * instead.
 */
abstract class JsonValueSource : ValueSource<JsonValue, JsonValueSource.Parameters> {
    /**
     * Parameters for [JsonValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The JSON input file.
         */
        val inputFile: RegularFileProperty
    }

    override fun obtain(): JsonValue? {
        return try {
            parameters.inputFile.get().asFile.inputStream().use { it.parseJson() }
        } catch (_: IOException) {
            null
        }
    }
}
