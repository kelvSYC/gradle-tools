package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import com.kelvsyc.gradle.ant.AntIsolation
import com.kelvsyc.gradle.ant.AntOperations
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Base of the typed Ant tasks: each declares its own inputs and outputs and describes the Ant work as [elements],
 * which are built at execution time and run through [AntOperations].
 *
 * Extend this class to define a reusable Ant task type. It takes no constructor parameters: the services it needs are
 * injected properties ([objects], [fileSystemOperations]), which subclasses may use too.
 */
abstract class AbstractAntTask : DefaultTask() {
    /** The Ant classpath: Ant, its antlibs and anything they need. */
    @get:Classpath
    abstract val antClasspath: ConfigurableFileCollection

    /** How Ant is isolated from the build. */
    @get:Internal
    abstract val isolation: Property<AntIsolation>

    /** Creates the [AntOperations] that runs the Ant work; available to subclasses. */
    @get:Inject
    protected abstract val objects: ObjectFactory

    /** Prepares the output before Ant runs; available to subclasses. */
    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    init {
        isolation.convention(AntIsolation.CLASSLOADER)
    }

    /**
     * The Ant tasks to run, in order. Called at execution, so providers and secrets resolve late.
     */
    protected abstract fun elements(): List<AntElement>

    /**
     * Prepares the task's output before Ant runs; does nothing by default.
     */
    protected open fun prepare() = Unit

    /**
     * Deletes and recreates [directory], so that no output of an earlier run (such as the output of a removed input)
     * survives and Ant's own up-to-date checks never skip work that Gradle decided to rerun.
     */
    protected fun clearDirectory(directory: DirectoryProperty) {
        val target = directory.get().asFile
        fileSystemOperations.delete { delete(target) }
        target.mkdirs()
    }

    /**
     * Returns [elements]; exists so that tests can inspect the Ant tree without running Ant.
     */
    internal fun buildElements(): List<AntElement> = elements()

    /**
     * Runs [prepare], then [elements] with [antClasspath], isolated as [isolation] says.
     */
    @TaskAction
    fun runAnt() {
        prepare()
        objects.newInstance(AntOperations::class.java).run(antClasspath, isolation.get(), elements())
    }

    /**
     * Returns a new element with the given [name] and [attributes]; null attribute values are left out.
     */
    protected fun element(name: String, vararg attributes: Pair<String, Any?>): AntElement =
        AntElement(name).also { element ->
            attributes.forEach { (key, value) -> if (value != null) element.attributes[key] = value }
        }
}
