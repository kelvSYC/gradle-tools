package com.kelvsyc.gradle.karakum.actions

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] that invokes the Karakum TypeScript-to-Kotlin external-declaration generator.
 *
 * Supports two invocation modes:
 * - **Direct mode** (default): passes each file in [Parameters.inputFiles] as `--input` and
 *   [Parameters.outputDirectory] as `--output`.
 * - **Config-file mode**: passes [Parameters.configFile] as `--config`; all other input options
 *   are ignored. Use this when Karakum configuration is complex enough to warrant a JSON config.
 */
abstract class RunKarakumAction @Inject constructor(
    private val exec: ExecOperations,
) : WorkAction<RunKarakumAction.Parameters> {

    /**
     * Parameters for [RunKarakumAction].
     */
    interface Parameters : WorkParameters {
        /**
         * Full invocation command tokens. The first token is used as the executable; any
         * remaining tokens are prepended to the CLI argument list before Karakum-specific flags
         * (e.g. `["npx", "karakum@1.2.3"]` runs `npx` with `karakum@1.2.3` as its first arg).
         */
        val karakumCommand: ListProperty<String>

        /**
         * TypeScript declaration files or directories to pass as `--input` arguments.
         * Ignored when [configFile] is set.
         */
        val inputFiles: ConfigurableFileCollection

        /**
         * Absolute path to a `karakum.config.json` file. When present, Karakum is invoked with
         * `--config <path>` and [inputFiles] / [outputDirectory] are not passed on the command line.
         */
        val configFile: Property<String>

        /**
         * Absolute path to the directory where Karakum writes generated Kotlin source files.
         * Ignored when [configFile] is set.
         */
        val outputDirectory: Property<String>
    }

    override fun execute() {
        val command = parameters.karakumCommand.get()
        val cliArgs = buildList {
            addAll(command.drop(1))
            if (parameters.configFile.isPresent) {
                add("--config")
                add(parameters.configFile.get())
            } else {
                parameters.inputFiles.forEach { file ->
                    add("--input")
                    add(file.absolutePath)
                }
                add("--output")
                add(parameters.outputDirectory.get())
            }
        }
        exec.exec {
            executable(command.first())
            args(cliArgs)
        }
    }
}
