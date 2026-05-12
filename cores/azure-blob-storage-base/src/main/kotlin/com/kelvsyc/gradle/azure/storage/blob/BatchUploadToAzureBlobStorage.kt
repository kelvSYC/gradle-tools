package com.kelvsyc.gradle.azure.storage.blob

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
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
 * Uploads a number of artifacts to Azure Blob Storage concurrently via [UploadBlobAction] submissions to
 * [WorkerExecutor.noIsolation]. Each artifact consists of a container name, blob name, and source file.
 *
 * Specify artifacts to upload using [registerArtifact]. The task fails if any artifact fails to be uploaded.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class BatchUploadToAzureBlobStorage @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /**
     * Build service managing the account-scoped Blob Service client.
     */
    @get:ServiceReference
    abstract val service: Property<BlobServiceClientBuildService>

    /**
     * Information about an artifact to be uploaded to Azure Blob Storage.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /** The name of the blob container. */
        abstract val containerName: Property<String>

        /** The name of the blob within the container. */
        abstract val blobName: Property<String>

        /** The local file to upload. */
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
            queue.submit(UploadBlobAction::class.java) { params ->
                params.service.set(this@BatchUploadToAzureBlobStorage.service)
                params.containerName.set(artifact.containerName)
                params.blobName.set(artifact.blobName)
                params.inputFile.set(artifact.inputFile)
            }
        }
    }
}
