package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import com.kelvsyc.gradle.ant.AntIsolation
import com.kelvsyc.gradle.ant.AntOperations
import com.kelvsyc.gradle.ant.AntScope
import com.kelvsyc.gradle.ant.AntTrees
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

/**
 * Base of the typed Ant tasks: each declares its own inputs and outputs and describes the Ant work in [elements] with
 * the Kotlin DSL ([AntScope]). The tree is built at execution time and run through [AntOperations].
 *
 * Extend this class to define a reusable Ant task type. It takes no constructor parameters: the services it needs are
 * injected properties ([objects], [fileSystemOperations]), which subclasses may use too.
 */
@DisableCachingByDefault(because = "Abstract base: each subclass declares whether its own work is cacheable")
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
     * Declares the Ant tasks to run, in order, with the Kotlin DSL. Called at execution, so providers and secrets
     * resolve late; read optional properties with `orNull`, since `null` attribute values are left out.
     */
    protected abstract fun AntScope.elements()

    /**
     * Prepares the task's output before Ant runs; does nothing by default.
     */
    protected open fun prepare() = Unit

    /**
     * Deletes and recreates [directory], so that no output of an earlier run (such as the output of a removed input)
     * survives and Ant's own up-to-date checks never skip work that Gradle decided to rerun.
     *
     * @throws InvalidUserDataException if [directory] holds any of the task's input files: clearing it would delete
     *   them (see [requireNoInputsIn])
     */
    protected fun clearDirectory(directory: DirectoryProperty) {
        requireNoInputsIn(directory)
        val target = directory.get().asFile
        fileSystemOperations.delete { delete(target) }
        target.mkdirs()
    }

    /**
     * Fails before anything is deleted if [directory] is, or contains, any of the task's input files, such as an
     * output directory set to the source directory or to the directory holding the inputs. Call it before replacing
     * the contents of [directory] in any other way (as a `sync` into it does).
     *
     * @throws InvalidUserDataException naming the first input file found in [directory]
     */
    protected fun requireNoInputsIn(directory: DirectoryProperty) {
        val target = directory.get().asFile.toPath().toAbsolutePath().normalize()
        val input = inputs.files.files.firstOrNull { it.toPath().toAbsolutePath().normalize().startsWith(target) }
        if (input != null) {
            throw InvalidUserDataException(
                "The output directory $target of $path holds its input $input, which preparing the output would " +
                    "delete: use a directory of its own for the output."
            )
        }
    }

    /**
     * Fails if two of [files] have the same name, since writing their outputs into one directory by name would make
     * one overwrite the other.
     *
     * @param what what the files are, for the message (for example `"files to checksum"`)
     * @throws InvalidUserDataException naming the clashing files
     */
    protected fun requireDistinctNames(files: Collection<File>, what: String) {
        val clashes = files.groupBy { it.name }.values.filter { it.size > 1 }
        if (clashes.isNotEmpty()) {
            throw InvalidUserDataException(
                "The $what of $path include files with the same name, whose outputs would overwrite each other: " +
                    clashes.joinToString("; ") { group -> group.joinToString(", ") }
            )
        }
    }

    /**
     * Returns the tree of [elements]; also lets tests inspect it without running Ant.
     */
    internal fun buildElements(): List<AntElement> = AntTrees.build { elements() }

    /**
     * Runs [prepare], then [elements] with [antClasspath], isolated as [isolation] says.
     */
    @TaskAction
    fun runAnt() {
        prepare()
        objects.newInstance(AntOperations::class.java).run(antClasspath, isolation.get(), buildElements())
    }
}
