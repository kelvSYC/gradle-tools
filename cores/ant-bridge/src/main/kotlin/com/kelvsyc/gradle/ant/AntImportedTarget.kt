package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntTargetAction
import com.kelvsyc.gradle.ant.internal.PropertiesFiles
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

/**
 * Runs one target of a build imported with `antBridge.importBuild`, and only that target: its Ant dependencies run as
 * the tasks it depends on. The run starts from a fresh Ant project that gets the [seeds], parses the
 * [topLevelBuildFiles] and loads the [upstreamExports]; the properties the target newly defines are written to
 * [exportedPropertiesFile]. Like the legacy `AntTarget`, it is never up to date.
 *
 * Registered and wired by `antBridge.importBuild`; abstract only so that Gradle can generate its managed properties,
 * and not designed to be extended.
 */
@DisableCachingByDefault(because = "Runs an Ant target, whose side effects are undeclared outputs")
abstract class AntImportedTarget @Inject constructor(
    private val workers: WorkerExecutor,
    layout: ProjectLayout
) : DefaultTask() {
    /** The name of the Ant target to run. */
    @get:Input
    abstract val target: Property<String>

    /** Every file of the project's Ant build: the top-level files and every file they imported or included. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildFiles: ConfigurableFileCollection

    /** The top-level build files, in import order; their contents are inputs through [buildFiles]. */
    @get:Internal
    abstract val topLevelBuildFiles: ListProperty<File>

    /** The base directory of each of [topLevelBuildFiles], in the same order; an empty string means none is set. */
    @get:Internal
    abstract val baseDirectories: ListProperty<String>

    /** Properties set before the build files are parsed, as Ant user properties. */
    @get:Input
    abstract val seeds: MapProperty<String, String>

    /** The export files of the upstream targets, in Ant's order, loaded before the target runs. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val upstreamExports: ListProperty<RegularFile>

    /**
     * The file the properties the target newly defines are written to (empty if there are none). Defaults to
     * `build/ant-bridge/import/<task name>.properties`.
     */
    @get:OutputFile
    abstract val exportedPropertiesFile: RegularFileProperty

    /** The Ant classpath: Ant, its antlibs and anything they need. */
    @get:Classpath
    abstract val antClasspath: ConfigurableFileCollection

    /** How Ant is isolated from the build. */
    @get:Internal
    abstract val isolation: Property<AntIsolation>

    /**
     * The properties the target newly defined, read from [exportedPropertiesFile]. Carries the dependency on this
     * task.
     */
    @get:Internal
    val exportedProperties: Provider<Map<String, String>>
        get() = exportedPropertiesFile.map { PropertiesFiles.read(it.asFile) }

    init {
        group = "ant"
        isolation.convention(AntIsolation.CLASSLOADER)
        exportedPropertiesFile.convention(layout.buildDirectory.file("ant-bridge/import/$name.properties"))
        outputs.upToDateWhen { false }
    }

    /**
     * Deletes [exportedPropertiesFile], then runs the target through the Worker API, isolated as [isolation] says.
     */
    @TaskAction
    fun run() {
        exportedPropertiesFile.get().asFile.delete()
        // The worker classpath stays empty: the action builds its own isolated loader from the Ant classpath.
        val queue = when (isolation.get()) {
            AntIsolation.CLASSLOADER -> workers.classLoaderIsolation {}
            AntIsolation.PROCESS -> workers.processIsolation {}
        }
        queue.submit(AntTargetAction::class.java) {
            buildFiles.set(topLevelBuildFiles)
            baseDirectories.set(this@AntImportedTarget.baseDirectories)
            seeds.set(this@AntImportedTarget.seeds)
            upstreamExports.set(this@AntImportedTarget.upstreamExports)
            target.set(this@AntImportedTarget.target)
            exportFile.set(this@AntImportedTarget.exportedPropertiesFile)
            antClasspath.from(this@AntImportedTarget.antClasspath)
        }
    }
}
