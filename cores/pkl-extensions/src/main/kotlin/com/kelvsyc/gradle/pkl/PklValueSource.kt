package com.kelvsyc.gradle.pkl

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.pkl.core.Evaluator
import org.pkl.core.ModuleSource
import org.pkl.core.PModule
import org.pkl.core.PklException
import java.io.IOException

/**
 * Gradle [ValueSource] that provides a [PModule] evaluated from a Pkl file.
 *
 * The evaluator is preconfigured with default security settings, allowing local file imports
 * and the Pkl standard library.
 *
 * If the input file is not found or cannot be evaluated, no value will be provided.
 */
abstract class PklValueSource : ValueSource<PModule, PklValueSource.Parameters> {
    /**
     * Parameters for [PklValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The Pkl input file.
         */
        val inputFile: RegularFileProperty
    }

    override fun obtain(): PModule? {
        return try {
            Evaluator.preconfigured().use { evaluator ->
                evaluator.evaluate(ModuleSource.path(parameters.inputFile.get().asFile.toPath()))
            }
        } catch (_: PklException) {
            null
        } catch (_: IOException) {
            null
        }
    }
}
