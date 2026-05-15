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
