package com.kelvsyc.gradle.pkl

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.pkl.core.PModule
import org.pkl.core.PObject

/**
 * [ValueSource] that extracts a single string value from a Pkl file using a
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
abstract class PklPathValueSource : AbstractPklValueSource<String, PklPathValueSource.Parameters>() {
    /**
     * Parameters for [PklPathValueSource].
     */
    interface Parameters : AbstractPklValueSource.Parameters {
        /**
         * The dot-notation path expression to evaluate.
         *
         * A dot-separated list of property names, starting from the module root. For example,
         * `"database.host"` navigates to the `database` property, then to its `host` property.
         * Each segment must be a non-empty property name.
         */
        val pklPath: Property<String>
    }

    override fun doObtain(module: PModule): String? {
        val segments = parameters.pklPath.get().split(".")
        val result = segments.fold<String, Any?>(module) { current, segment ->
            (current as? PObject)?.properties?.get(segment)
        }
        return when (result) {
            is String -> result
            is Long -> result.toString()
            is Double -> result.toString()
            is Boolean -> result.toString()
            else -> null
        }
    }
}
