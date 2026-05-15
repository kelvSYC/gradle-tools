package com.kelvsyc.gradle.providers

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException
import java.util.*

/**
 * Gradle [ValueSource] that provides a [Properties] object from reading a properties file.
 *
 * **Configuration cache:** The entire [Properties] result returned by [obtain] is serialized to the Gradle
 * configuration cache in plaintext when the cache is written. If the file contains sensitive values — passwords,
 * tokens, API keys — those values will be stored in `.gradle/configuration-cache/` and are readable by any
 * process with access to the build directory. Only use this source with files whose complete contents can be
 * safely cached. When sensitive values must be available at task execution time, read the file inside a
 * [org.gradle.workers.WorkAction] instead.
 *
 * If the input file is not found, or there is an error reading the properties file, no value will be provided.
 */
abstract class PropertiesFromFileValueSource : ValueSource<Properties, PropertiesFromFileValueSource.Parameters> {
    /**
     * Parameters for [PropertiesFromFileValueSource]
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The input file.
         */
        val inputFile: RegularFileProperty
    }

    override fun obtain(): Properties? {
        return try {
            parameters.inputFile.get().asFile.reader().use {
                Properties().apply {
                    load(it)
                }
            }
        } catch (_: IOException) {
            // Error reading file
            null
        } catch (_: IllegalArgumentException) {
            // Malformed unicode encoding detected
            null
        }
    }
}
