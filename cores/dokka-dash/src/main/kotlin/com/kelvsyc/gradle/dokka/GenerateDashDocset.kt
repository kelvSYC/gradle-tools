package com.kelvsyc.gradle.dokka

import com.kelvsyc.gradle.dokka.actions.GenerateDashDocsetAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Generates a [Dash](https://kapeli.com/dash) docset from the HTML output of a Dokka documentation build.
 *
 * The task copies the Dokka HTML tree into the docset bundle, writes an `Info.plist`, and builds
 * a SQLite search index from Dokka's `navigation.json`. The actual assembly runs inside
 * [GenerateDashDocsetAction] under process isolation so that the sqlite-jdbc native library loads in a dedicated JVM
 * rather than the Gradle daemon.
 */
@CacheableTask
abstract class GenerateDashDocset @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {

    /** The root directory of an existing Dokka HTML output. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dokkaOutputDirectory: DirectoryProperty

    /** Base name of the produced bundle (written as `<docsetName>.docset`) and the display name shown in Dash. */
    @get:Input
    abstract val docsetName: Property<String>

    /**
     * Reverse-DNS identifier for the docset (e.g. `com.example.mylibrary`).
     *
     * Populates `CFBundleIdentifier` in `Info.plist`. The lowercased value is also used as
     * `DocSetPlatformFamily`, which controls icon selection in Dash.
     */
    @get:Input
    abstract val bundleIdentifier: Property<String>

    /**
     * Relative path of the page Dash opens when the docset is selected.
     *
     * When absent, defaults to `index.html`, which is the root page of a Dokka HTML output.
     */
    @get:Input
    @get:Optional
    abstract val indexPage: Property<String>

    /** Parent directory that will contain the `.docset` bundle. See [docsetDirectory] for the bundle path itself. */
    @get:Internal
    abstract val outputDirectory: DirectoryProperty

    /**
     * The `.docset` bundle directory produced by this task, computed as `<outputDirectory>/<docsetName>.docset`.
     *
     * Use this property to wire the docset as an input to downstream tasks rather than
     * reconstructing the path manually from [outputDirectory] and [docsetName].
     */
    @get:OutputDirectory
    val docsetDirectory: Provider<Directory>
        get() = docsetName.flatMap { name -> outputDirectory.dir("$name.docset") }

    /**
     * Classpath passed to the worker process.
     *
     * Must contain the sqlite-jdbc JAR. Wired automatically by [com.kelvsyc.gradle.plugins.DokkaDashPlugin]
     * via the `dokkaDashWorkerClasspath` dependency scope.
     */
    @get:Classpath
    abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    fun run() {
        val queue = workers.processIsolation {
            classpath.from(workerClasspath)
        }
        queue.submit(GenerateDashDocsetAction::class.java) {
            dokkaOutputDirectory.set(this@GenerateDashDocset.dokkaOutputDirectory)
            docsetName.set(this@GenerateDashDocset.docsetName)
            bundleIdentifier.set(this@GenerateDashDocset.bundleIdentifier)
            indexPage.set(this@GenerateDashDocset.indexPage)
            docsetDirectory.set(this@GenerateDashDocset.docsetDirectory)
        }
    }
}
