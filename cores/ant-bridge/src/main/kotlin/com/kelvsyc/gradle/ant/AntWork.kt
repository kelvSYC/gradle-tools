package com.kelvsyc.gradle.ant

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Runs an Ant tree, built at configuration time with [ant] (Kotlin DSL) or [record] (legacy `ant.xxx(...)` calls).
 *
 * Declare inputs and outputs with the normal `inputs`/`outputs` API; without declared outputs the task is never
 * up to date. With the `com.kelvsyc.gradle.ant-bridge` plugin, [antClasspath] defaults to the `antBridgeClasspath`
 * configuration and [isolation] to [AntIsolation.CLASSLOADER].
 */
@DisableCachingByDefault(because = "Arbitrary Ant work; declare outputs to get up-to-date checks")
abstract class AntWork @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    /** The Ant tasks to run, in order; values stay unresolved until execution. */
    @get:Internal
    abstract val elements: ListProperty<AntElement>

    /** The Ant classpath: Ant, its antlibs and anything they need. */
    @get:Classpath
    abstract val antClasspath: ConfigurableFileCollection

    /** How Ant is isolated from the build. */
    @get:Internal
    abstract val isolation: Property<AntIsolation>

    private val operations = objects.newInstance(AntOperations::class.java)

    /**
     * Appends the Ant tasks of [body] (Kotlin DSL) to [elements].
     */
    fun ant(body: Action<in AntScope>) {
        elements.addAll(buildTree(body))
    }

    /**
     * Appends the legacy `ant.xxx(...)` calls recorded by [body] to [elements].
     */
    fun record(body: Action<in AntRecorder>) {
        elements.addAll(AntRecorder.record { body.execute(it) })
    }

    /**
     * Runs [elements] with [antClasspath], isolated as [isolation] says.
     */
    @TaskAction
    fun run() {
        operations.run(antClasspath, isolation.get(), elements.get())
    }
}
