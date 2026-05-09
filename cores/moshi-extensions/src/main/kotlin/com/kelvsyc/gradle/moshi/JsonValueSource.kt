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
