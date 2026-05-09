package com.kelvsyc.gradle.moshi

import com.kelvsyc.kotlin.moshi.JsonPath
import com.kelvsyc.kotlin.moshi.parseJson
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException

/**
 * Gradle [ValueSource] that extracts a single string value from a JSON file using a JsonPath
 * expression.
 *
 * The matched value is coerced to a string: strings are returned directly, numbers and booleans
 * are converted via [toString], and nulls or non-scalar values result in no value being provided.
 *
 * If the input file is not found, cannot be parsed, or the path matches zero or multiple nodes,
 * no value will be provided.
 */
abstract class JsonPathValueSource : ValueSource<String, JsonPathValueSource.Parameters> {
    /**
     * Parameters for [JsonPathValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The JSON input file.
         */
        val inputFile: RegularFileProperty

        /**
         * The JsonPath expression to evaluate.
         */
        val jsonPath: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val root = parameters.inputFile.get().asFile.inputStream().use { it.parseJson() }
            val path = JsonPath.parse(parameters.jsonPath.get())
            val result = path.queryOne(root) ?: return null

            result.asString()
                ?: result.asNumber()?.toString()
                ?: result.asBoolean()?.toString()
        } catch (_: IOException) {
            null
        }
    }
}
