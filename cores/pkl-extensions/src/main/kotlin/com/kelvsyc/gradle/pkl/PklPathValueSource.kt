package com.kelvsyc.gradle.pkl

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.pkl.core.Evaluator
import org.pkl.core.ModuleSource
import org.pkl.core.PObject
import org.pkl.core.PklException
import java.io.IOException

/**
 * Gradle [ValueSource] that extracts a single string value from a Pkl file using a
 * dot-notation path expression.
 *
 * The path is a dot-separated list of property names (e.g., `"database.host"`). Navigation
 * starts from the module root and descends into nested [PObject] values at each segment.
 *
 * The matched value is coerced to a string: strings are returned directly, numbers and booleans
 * are converted via [toString], and all other types (objects, listings, null) result in no value
 * being provided.
 *
 * If the input file is not found, cannot be evaluated, or the path does not resolve to a scalar
 * value, no value will be provided.
 */
abstract class PklPathValueSource : ValueSource<String, PklPathValueSource.Parameters> {
    /**
     * Parameters for [PklPathValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The Pkl input file.
         */
        val inputFile: RegularFileProperty

        /**
         * The dot-notation path expression to evaluate.
         *
         * A dot-separated list of property names, starting from the module root. For example,
         * `"database.host"` navigates to the `database` property, then to its `host` property.
         * Each segment must be a non-empty property name.
         */
        val pklPath: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val module = Evaluator.preconfigured().use { evaluator ->
                evaluator.evaluate(ModuleSource.path(parameters.inputFile.get().asFile.toPath()))
            }
            val segments = parameters.pklPath.get().split(".")
            val result = segments.fold<String, Any?>(module) { current, segment ->
                (current as? PObject)?.properties?.get(segment)
            }

            when (result) {
                is String -> result
                is Long -> result.toString()
                is Double -> result.toString()
                is Boolean -> result.toString()
                else -> null
            }
        } catch (_: PklException) {
            null
        } catch (_: IOException) {
            null
        }
    }
}
