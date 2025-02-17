package com.kelvsyc.gradle.providers

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException
import java.util.*

/**
 * Gradle [ValueSource] that provides a [Properties] object from reading a properties file.
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
            null
        }
    }
}
