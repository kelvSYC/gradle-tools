package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.actions.AntWorkAction
import com.kelvsyc.gradle.ant.internal.PropertiesFiles
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

/**
 * Runs Ant from a task action, modelled on Gradle's `ExecOperations`. Each call builds an Ant tree, resolves it,
 * runs it through the Worker API and waits for it to finish, so it is safe to call at execution time and with the
 * configuration cache. The Ant project's base directory is the project directory.
 *
 * Obtain one from `antBridge.operations`, or create one in a task with `ObjectFactory.newInstance`.
 * A failing Ant task fails the call with Gradle's `WorkerExecutionException`, caused by [AntExecutionException].
 *
 * Each call waits for all work the current task has submitted, not only its own. To run Ant next to other work items
 * and wait once, submit [com.kelvsyc.gradle.ant.actions.AntWorkAction] to your own work queue instead.
 *
 * The overloads taking `results` return the requested Ant properties as the tree left them; names the tree did not
 * set are absent from the map. To hand the values to another task, write them to a declared output of your task.
 */
abstract class AntOperations @Inject constructor(
    private val workers: WorkerExecutor,
    private val layout: ProjectLayout
) {
    /**
     * Runs the Ant tasks of [body] (Kotlin DSL) with [classpath], in an isolated class loader.
     */
    fun execute(classpath: FileCollection, body: Action<in AntScope>) =
        execute(classpath, AntIsolation.CLASSLOADER, body)

    /**
     * Runs the Ant tasks of [body] (Kotlin DSL) with [classpath], isolated as [isolation] says.
     */
    fun execute(classpath: FileCollection, isolation: AntIsolation, body: Action<in AntScope>) =
        run(classpath, isolation, AntTrees.build(body))

    /**
     * Runs the legacy `ant.xxx(...)` calls recorded by [body] with [classpath], in an isolated class loader.
     */
    fun record(classpath: FileCollection, body: Action<in AntRecorder>) =
        record(classpath, AntIsolation.CLASSLOADER, body)

    /**
     * Runs the legacy `ant.xxx(...)` calls recorded by [body] with [classpath], isolated as [isolation] says.
     */
    fun record(classpath: FileCollection, isolation: AntIsolation, body: Action<in AntRecorder>) =
        run(classpath, isolation, AntTrees.record(body))

    /**
     * Runs the Ant tasks of [body] (Kotlin DSL) with [classpath], isolated as [isolation] says, and returns the
     * values of the Ant properties named in [results] that the tree set.
     */
    fun execute(
        classpath: FileCollection,
        isolation: AntIsolation,
        results: Set<String>,
        body: Action<in AntScope>
    ): Map<String, String> = runForResults(classpath, isolation, AntTrees.build(body), results)

    /**
     * Runs the legacy `ant.xxx(...)` calls recorded by [body] with [classpath], isolated as [isolation] says, and
     * returns the values of the Ant properties named in [results] that the tree set.
     */
    fun record(
        classpath: FileCollection,
        isolation: AntIsolation,
        results: Set<String>,
        body: Action<in AntRecorder>
    ): Map<String, String> = runForResults(classpath, isolation, AntTrees.record(body), results)

    /**
     * Resolves [elements] and runs them with [classpath], isolated as [isolation] says, waiting for completion.
     */
    fun run(classpath: FileCollection, isolation: AntIsolation, elements: List<AntElement>) =
        run(classpath, isolation, elements, emptySet(), null)

    /**
     * Resolves [elements] and runs them with [classpath], isolated as [isolation] says, waiting for completion.
     * When [resultFile] is given, the work writes the Ant properties named in [results] to it.
     */
    internal fun run(
        classpath: FileCollection,
        isolation: AntIsolation,
        elements: List<AntElement>,
        results: Set<String>,
        resultFile: File?
    ) {
        val resolved = elements.map { it.resolve() }
        // The worker classpath stays empty: the action builds its own isolated loader from the Ant classpath.
        val queue = when (isolation) {
            AntIsolation.CLASSLOADER -> workers.classLoaderIsolation {}
            AntIsolation.PROCESS -> workers.processIsolation {}
        }
        queue.submit(AntWorkAction::class.java) {
            nodes.set(resolved)
            antClasspath.from(classpath)
            baseDirectory.set(layout.projectDirectory)
            resultProperties.set(results)
            if (resultFile != null) {
                this.resultFile.set(resultFile)
            }
        }
        workers.await()
    }

    private fun runForResults(
        classpath: FileCollection,
        isolation: AntIsolation,
        elements: List<AntElement>,
        results: Set<String>
    ): Map<String, String> {
        val file = Files.createTempFile("ant-bridge", ".properties").toFile()
        try {
            run(classpath, isolation, elements, results, file)
            return PropertiesFiles.read(file)
        } finally {
            file.delete()
        }
    }
}
