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
 * This task uploads a number of artifacts to Artifactory. Each artifact consists of a repository key and path,
 * as well as a source file. All artifacts must be supplied with a name for easy reference.
 *
 * Specify artifacts to upload using [registerArtifact].
 *
 * All artifacts are uploaded concurrently via [UploadArtifactAction].
 *
 * **Note:** This task is intended for use with non-Maven, non-Ivy Artifactory repositories (e.g. generic
 * repositories). For Maven or Ivy repositories, prefer Gradle's built-in publishing mechanisms (e.g.
 * `maven-publish`) instead.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class AbstractBatchUploadToArtifactory @Inject constructor(
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
     * Information about an artifact to be uploaded to Artifactory.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * The Artifactory repository key.
         */
        abstract val repository: Property<String>

        /**
         * The path within the repository to which the artifact will be uploaded.
         */
        abstract val path: Property<String>

        /**
         * The local file to upload.
         */
        abstract val inputFile: RegularFileProperty
    }

    /**
     * The artifacts to be uploaded.
     *
     * Users generally add to this collection through [registerArtifact].
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

    @Suppress("LeakingThis")
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    val inputFiles = artifacts.map { it.mapValues { it.value.inputFile.get() } }

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        artifacts.get().forEach { (_, artifact) ->
            queue.submit(UploadArtifactAction::class.java) {
                service.set(this@AbstractBatchUploadToArtifactory.service)
                clientName.set(this@AbstractBatchUploadToArtifactory.clientName)
                repository.set(artifact.repository)
                path.set(artifact.path)
                inputFile.set(artifact.inputFile)
            }
        }
    }
}
