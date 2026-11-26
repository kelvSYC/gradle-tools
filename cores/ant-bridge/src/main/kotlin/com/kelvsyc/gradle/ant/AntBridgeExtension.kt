package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntImportRegistry
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskContainer
import java.io.File
import javax.inject.Inject

/**
 * The `antBridge` extension. Not designed to be extended.
 */
abstract class AntBridgeExtension @Inject constructor(
    /** The resolved Ant classpath: Ant, its antlibs and anything else declared on `antBridge`. */
    val classpath: FileCollection,
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val layout: ProjectLayout,
    tasks: TaskContainer
) {
    /** Runs Ant from task actions and `doLast` blocks at execution time. */
    val operations: AntOperations = objects.newInstance(AntOperations::class.java)

    /** The project's imported Ant build. */
    internal val imports = AntImportRegistry(tasks, layout, providers, objects, classpath)

    /**
     * Returns the properties declared by [configure], loaded by [AntProperties] with [classpath]. Each obtain opens
     * and closes its own class loader.
     */
    fun properties(configure: Action<in AntPropertiesSpec>): Provider<Map<String, String>> {
        val spec = objects.newInstance(AntPropertiesSpec::class.java)
        configure.execute(spec)
        return providers.of(AntProperties::class.java) {
            parameters {
                spec.applyTo(this)
                antClasspath.from(classpath)
            }
        }
    }

    /**
     * Returns the properties requested by [configure], set by its Ant tree, run by [AntQuery] with [classpath]. Only
     * read-only Ant tasks are allowed unless [AntQuerySpec.allowSideEffects] is called, checked here, before anything
     * runs. Read during configuration, the tree re-runs on every build (cache check); read only in task actions, it
     * is obtained when the task runs. Each obtain opens and closes its own class
     * loader.
     *
     * @throws org.gradle.api.InvalidUserDataException if the tree has a task that is not allowed
     */
    fun query(configure: Action<in AntQuerySpec>): Provider<Map<String, String>> {
        val spec = objects.newInstance(AntQuerySpec::class.java)
        configure.execute(spec)
        return providers.of(AntQuery::class.java) {
            parameters {
                spec.applyTo(this)
                antClasspath.from(classpath)
            }
        }
    }

    /**
     * Imports the Ant build [file] (resolved as `project.file` does), the replacement for Gradle's `ant.importBuild`;
     * see the overload taking a configuration.
     */
    fun importBuild(file: Any): AntImport = importBuild(file) {}

    /**
     * Imports the Ant build [file] (resolved as `project.file` does), configured by [configure], and returns its
     * handle. Its top-level statements and targets are based at the file's directory; the file's own
     * `<project basedir>` is ignored, as legacy `importBuild` ignored it.
     *
     * The call reads the project's whole Ant build (every file imported so far, in call order) with Ant's parser and
     * registers an [AntImportedTarget] for each target the file adds, except excluded ones, so `tasks.named(...)`
     * works right after it. Each task depends on the tasks named by its target's `depends` (a name that is not a
     * target is a Gradle task name), each of those tasks should run after the one named before it in that `depends`,
     * as with legacy, and each task loads the properties of its upstream targets, as Ant would order them, from
     * their export files, which are declared inputs. Dependencies and exports are resolved from the final build,
     * after every call.
     *
     * Reading runs the files' top-level statements during configuration; every file Ant loaded, and every file its
     * top-level `property file=`, `loadproperties` and optional `<import>` read or looked for (missing or not), is a
     * configuration cache input.
     *
     * @throws org.gradle.api.InvalidUserDataException if a task name is taken, or an exclusion names a target the
     * file does not add
     * @throws AntExecutionException if Ant fails to read the build, or a target is defined in two top-level files
     */
    fun importBuild(file: Any, configure: Action<in AntImportSpec>): AntImport =
        readImport(layout.files(file).singleFile, null, configure)

    /**
     * Imports the Ant build [file], with its top-level statements and targets based at [baseDirectory] (both
     * resolved as `project.file` does; the file's own `<project basedir>` is ignored, as legacy `importBuild` ignored
     * it), configured by [configure]; see the overload without a base directory.
     */
    fun importBuild(file: Any, baseDirectory: Any, configure: Action<in AntImportSpec>): AntImport =
        readImport(layout.files(file).singleFile, layout.files(baseDirectory).singleFile, configure)

    /** Logs a warning for each excluded target with imported targets downstream and no supplied properties. */
    internal fun warnAboutUnsuppliedExclusions() = imports.warnAboutUnsuppliedExclusions()

    private fun readImport(file: File, baseDirectory: File?, configure: Action<in AntImportSpec>): AntImport {
        val spec = objects.newInstance(AntImportSpec::class.java)
        configure.execute(spec)
        return imports.importBuild(file, baseDirectory, spec)
    }
}
