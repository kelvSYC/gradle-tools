package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import com.kelvsyc.gradle.ant.AntIsolation
import com.kelvsyc.gradle.ant.AntOperations
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Base of the typed Ant tasks: each declares its own inputs and outputs and describes the Ant work as [elements],
 * which are built at execution time and run through [AntOperations].
 */
abstract class AbstractAntTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    /** The Ant classpath: Ant, its antlibs and anything they need. */
    @get:Classpath
    abstract val antClasspath: ConfigurableFileCollection

    /** How Ant is isolated from the build. */
    @get:Internal
    abstract val isolation: Property<AntIsolation>

    private val operations = objects.newInstance(AntOperations::class.java)

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
     * Returns [elements]; exists so that tests can inspect the Ant tree without running Ant.
     */
    internal fun buildElements(): List<AntElement> = elements()

    /**
     * Runs [prepare], then [elements] with [antClasspath], isolated as [isolation] says.
     */
    @TaskAction
    fun runAnt() {
        prepare()
        operations.run(antClasspath, isolation.get(), elements())
    }

    /**
     * Returns a new element with the given [name] and [attributes]; null attribute values are left out.
     */
    protected fun element(name: String, vararg attributes: Pair<String, Any?>): AntElement =
        AntElement(name).also { element ->
            attributes.forEach { (key, value) -> if (value != null) element.attributes[key] = value }
        }
}
