package com.kelvsyc.gradle.snakeyaml

import com.kelvsyc.kotlin.snakeyaml.YamlValue
import com.kelvsyc.kotlin.snakeyaml.parseYaml
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.io.IOException

/**
 * Gradle [ValueSource] that provides a [YamlValue] tree parsed from a YAML file.
 *
 * If the input file is not found, cannot be read, or cannot be parsed, no value will be provided.
 *
 * **Configuration cache and sensitive files:** Gradle serializes the entire [YamlValue] tree to the configuration
 * cache in plaintext when the cache is written. If the YAML file contains sensitive values — passwords, tokens, API
 * keys — those values will be stored in `.gradle/configuration-cache/`. This applies regardless of how the
 * resulting [org.gradle.api.provider.Provider] is stored: a task `@Input`, `@get:Internal`, or private `val` all
 * cause `obtain()` to run at configuration time and the parsed tree to be cached. For files containing sensitive
 * data, read and use the file entirely within a `@TaskAction` or [org.gradle.workers.WorkAction.execute] body
 * instead.
 */
abstract class YamlValueSource : ValueSource<YamlValue, YamlValueSource.Parameters> {
    /**
     * Parameters for [YamlValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The YAML input file.
         */
        val inputFile: RegularFileProperty
    }

    override fun obtain(): YamlValue? {
        return try {
            parameters.inputFile.get().asFile.inputStream().use { it.parseYaml() }
        } catch (_: IOException) {
            null
        } catch (_: YamlEngineException) {
            null
        }
    }
}
