package com.kelvsyc.gradle.nexus

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Downloads a set of artifacts from Nexus raw repositories concurrently.
 *
 * Each artifact is identified by a name, repository, and path. Specify artifacts using
 * [registerArtifact]. All artifacts are downloaded in parallel via [DownloadArtifactAction].
 *
 * Subclass this task if you need to manage the `service` property yourself (e.g. when composing
 * with a custom build service registration). For the common case, use [BatchDownloadFromNexus]
 * which adds `@get:ServiceReference` so Gradle tracks the service as a task dependency
 * automatically.
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AbstractBatchDownloadFromNexus @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Nexus client to use for all downloads in this task.
     */
    @get:Internal
    abstract val service: Property<NexusClientBuildService>

    /**
     * A single artifact entry registered for download.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * The name of the Nexus repository containing the artifact.
         */
        abstract val repository: Property<String>

        /**
         * The path to the artifact within the repository.
         */
        abstract val path: Property<String>

        /**
         * The local file to which the artifact will be written.
         */
        abstract val outputFile: RegularFileProperty
    }

    /**
     * The registered artifacts to download.
     *
     * Populated by [registerArtifact]; modifying this map directly is not recommended.
     */
    @get:Internal
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers a new artifact to be downloaded.
     */
    fun registerArtifact(name: String, configureAction: Action<in Artifact>) {
        artifacts.put(name, providers.provider {
            objects.newInstance<Artifact>(name).also { configureAction.execute(it) }
        })
    }

    /**
     * The output files produced by this task, keyed by artifact name.
     *
     * Provided as a convenience for wiring the output of this task to other task inputs.
     */
    @Suppress("LeakingThis")
    @get:OutputFiles
    val outputFiles = artifacts.map { it.mapValues { entry -> entry.value.outputFile.get() } }

    /** Downloads all registered artifacts concurrently. */
    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        artifacts.get().forEach { (_, artifact) ->
            queue.submit(DownloadArtifactAction::class.java) { params ->
                params.service.set(this@AbstractBatchDownloadFromNexus.service)
                params.repository.set(artifact.repository)
                params.path.set(artifact.path)
                params.outputFile.set(artifact.outputFile)
            }
        }
    }
}
