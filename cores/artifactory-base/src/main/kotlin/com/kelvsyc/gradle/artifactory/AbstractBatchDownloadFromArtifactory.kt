package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.ClientsBaseService
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
 * This task downloads a number of artifacts from Artifactory. Each artifact consists of a repository key and path,
 * as well as a download destination. All artifacts must be supplied with a name for easy reference.
 *
 * Specify artifacts to download using [registerArtifact].
 *
 * All artifacts are downloaded concurrently via [DownloadArtifactAction].
 *
 * **Note:** This task is intended for use with non-Maven, non-Ivy Artifactory repositories (e.g. generic
 * repositories). For Maven or Ivy repositories, prefer Gradle's built-in dependency resolution mechanisms instead.
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AbstractBatchDownloadFromArtifactory @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /**
     * The [ClientsBaseService] used to obtain the Artifactory client.
     */
    @get:Internal
    abstract val service: Property<ClientsBaseService>

    /**
     * Registered name of an [ArtifactoryClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>

    /**
     * Information about an artifact to be retrieved from Artifactory.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * The Artifactory repository key.
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
     * The artifacts to be downloaded.
     *
     * Users generally add to this collection through [registerArtifact].
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
     * The locations of the downloaded artifacts.
     *
     * This property is provided as a convenience for wiring the output of this task to other task inputs.
     */
    @Suppress("LeakingThis")
    @get:OutputFiles
    val outputFiles = artifacts.map { it.mapValues { it.value.outputFile.get() } }

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        artifacts.get().forEach { (_, artifact) ->
            queue.submit(DownloadArtifactAction::class.java) {
                service.set(this@AbstractBatchDownloadFromArtifactory.service)
                clientName.set(this@AbstractBatchDownloadFromArtifactory.clientName)
                repository.set(artifact.repository)
                path.set(artifact.path)
                outputFile.set(artifact.outputFile)
            }
        }
    }
}
