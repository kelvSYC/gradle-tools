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
 * Abstract base class for [ValueSource] implementations that evaluate a Pkl file.
 *
 * Subclasses implement [doObtain] to transform the evaluated [PModule] into a value of type [T].
 * This class handles file evaluation, error handling, and evaluator lifecycle.
 *
 * **Configuration cache compatibility is incomplete.** For this ValueSource to be compatible
 * with Gradle's configuration cache, [T] must implement [java.io.Serializable]. [PModule] itself
 * does not, so subclasses that return [PModule] directly will fail under `--configuration-cache`.
 * For a config-cache-safe alternative that extracts scalar string values, use [PklPathValueSource].
 */
abstract class AbstractPklValueSource<T : Any, P : AbstractPklValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractPklValueSource].
     *
     * Extend this interface to supply additional parameters to the subclass.
     */
    interface Parameters : ValueSourceParameters {
        /** The Pkl input file to evaluate. */
        val inputFile: RegularFileProperty
    }

    /**
     * Transforms the evaluated [PModule] into the desired value.
     *
     * Called once per [obtain] invocation, after successful evaluation of the Pkl file.
     *
     * @param module the evaluated Pkl module
     * @return the extracted value, or `null` if the module does not contain the expected data
     */
    abstract fun doObtain(module: PModule): T?

    override fun obtain(): T? {
        return try {
            val module = Evaluator.preconfigured().use { evaluator ->
                evaluator.evaluate(ModuleSource.path(parameters.inputFile.get().asFile.toPath()))
            }
            doObtain(module)
        } catch (_: PklException) {
            null
        } catch (_: IOException) {
            null
        }
    }
}
