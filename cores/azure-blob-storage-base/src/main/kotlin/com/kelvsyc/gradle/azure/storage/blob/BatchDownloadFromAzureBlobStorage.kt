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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Downloads a number of blobs from Azure Blob Storage concurrently via [DownloadBlobAction] submissions to
 * [WorkerExecutor.noIsolation]. Each artifact consists of a container name, blob name, and download destination.
 *
 * Specify artifacts to download using [registerArtifact]. The task fails if any artifact fails to be retrieved.
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchDownloadFromAzureBlobStorage @Inject constructor(
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
     * Information about an artifact to be retrieved from Azure Blob Storage.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /** The name of the blob container. */
        abstract val containerName: Property<String>

        /** The name of the blob within the container. */
        abstract val blobName: Property<String>

        /** The local file to download the blob to. */
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
            queue.submit(DownloadBlobAction::class.java) { params ->
                params.service.set(this@BatchDownloadFromAzureBlobStorage.service)
                params.containerName.set(artifact.containerName)
                params.blobName.set(artifact.blobName)
                params.outputFile.set(artifact.outputFile)
            }
        }
    }
}
