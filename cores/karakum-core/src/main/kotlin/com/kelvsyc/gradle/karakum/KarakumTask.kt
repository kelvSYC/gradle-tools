package com.kelvsyc.gradle.karakum

import com.kelvsyc.gradle.karakum.actions.RunKarakumAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task that invokes the Karakum TypeScript-to-Kotlin external-declaration generator as part of
 * the Gradle build.
 *
 * Supports two invocation modes:
 * - **Direct mode**: declare [inputFiles] (TypeScript `.d.ts` files or directories) and
 *   [outputDirectory]; Karakum is called with `--input`/`--output` flags.
 * - **Config-file mode**: set [configFile] to a `karakum.config.json`; all input routing is
 *   delegated to that file.
 *
 * The [karakumCommand] property defaults to the convention supplied by [com.kelvsyc.gradle.plugins.KarakumPlugin]
 * (system `npx`, or the KMP-managed `npx` when a JS target is detected). Override it or call a
 * preset method on the `karakum` extension to change the invocation strategy.
 *
 * The implementation delegates to [RunKarakumAction] via [WorkerExecutor.noIsolation].
 */
@CacheableTask
abstract class KarakumTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {

    /**
     * TypeScript declaration files or directories to pass as `--input` arguments.
     * Ignored when [configFile] is set.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFiles: ConfigurableFileCollection

    /**
     * Optional path to a `karakum.config.json` file. When set, Karakum is invoked in config-file
     * mode and [inputFiles] / [outputDirectory] are not passed on the command line.
     */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configFile: RegularFileProperty

    /**
     * Directory into which Karakum writes the generated Kotlin external declarations.
     * Automatically wired into the appropriate Kotlin source set by [com.kelvsyc.gradle.plugins.KarakumPlugin].
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * Full invocation command tokens (e.g. `["npx", "karakum@1.2.3"]`). Provided by the plugin
     * extension convention; override here or via the `karakum` extension for project-wide changes.
     *
     * @see [RunKarakumAction.Parameters.karakumCommand]
     */
    @get:Internal
    abstract val karakumCommand: ListProperty<String>

    @TaskAction
    fun run() {
        workers.noIsolation().submit(RunKarakumAction::class) {
            karakumCommand.set(this@KarakumTask.karakumCommand)
            inputFiles.from(this@KarakumTask.inputFiles)
            if (this@KarakumTask.configFile.isPresent) {
                configFile.set(this@KarakumTask.configFile.get().asFile.absolutePath)
            }
            outputDirectory.set(this@KarakumTask.outputDirectory.get().asFile.absolutePath)
        }
    }
}
