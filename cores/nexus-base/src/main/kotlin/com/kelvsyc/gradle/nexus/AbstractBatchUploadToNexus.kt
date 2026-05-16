package com.kelvsyc.gradle.nexus

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Uploads a set of artifacts to Nexus raw repositories concurrently.
 *
 * Each artifact is identified by a name, repository, and path. Specify artifacts using
 * [registerArtifact]. All artifacts are uploaded in parallel via [UploadArtifactAction].
 *
 * Subclass this task if you need to manage the `service` property yourself. For the common case,
 * use [BatchUploadToNexus] which adds `@get:ServiceReference` so Gradle tracks the service as a
 * task dependency automatically.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class AbstractBatchUploadToNexus @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Nexus client to use for all uploads in this task.
     */
    @get:Internal
    abstract val service: Property<NexusClientBuildService>

    /**
     * A single artifact entry registered for upload.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * The name of the Nexus repository to upload to.
         */
        abstract val repository: Property<String>

        /**
         * The target path within the repository (e.g. `com/example/1.0/artifact-1.0.jar`).
         */
        abstract val path: Property<String>

        /**
         * The local file to upload.
         */
        abstract val inputFile: RegularFileProperty
    }

    /**
     * The registered artifacts to upload.
     *
     * Populated by [registerArtifact]; modifying this map directly is not recommended.
     */
    @get:Internal
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers a new artifact to be uploaded.
     */
    fun registerArtifact(name: String, configureAction: Action<in Artifact>) {
        artifacts.put(name, providers.provider {
            objects.newInstance<Artifact>(name).also { configureAction.execute(it) }
        })
    }

    /**
     * The input files consumed by this task, keyed by artifact name.
     *
     * Provided as a convenience for wiring other task outputs to this task's inputs.
     */
    @Suppress("LeakingThis")
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    val inputFiles = artifacts.map { it.mapValues { entry -> entry.value.inputFile.get() } }

    /** Uploads all registered artifacts concurrently. */
    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        artifacts.get().forEach { (_, artifact) ->
            queue.submit(UploadArtifactAction::class.java) { params ->
                params.service.set(this@AbstractBatchUploadToNexus.service)
                params.repository.set(artifact.repository)
                params.path.set(artifact.path)
                params.inputFile.set(artifact.inputFile)
            }
        }
    }
}
