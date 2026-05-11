package com.kelvsyc.gradle.azure.storage.blob

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
 * This task retrieves a number of blobs from Azure Blob Storage. Each artifact consists of a container name, blob name,
 * and a download destination. All artifacts must be supplied with a name for easy references.
 *
 * Specify artifacts to download using [registerArtifact].
 *
 * All registered artifacts will be retrieved concurrently via [DownloadBlobAction] submissions to
 * [WorkerExecutor.noIsolation]. This task fails if any artifact fails to be retrieved.
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AbstractBatchDownloadFromAzureBlobStorage @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /**
     * The [ClientsBaseService] used to obtain the Azure Blob Storage client.
     */
    @get:Internal
    abstract val service: Property<ClientsBaseService>

    /**
     * Registered name of a [BlobServiceClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>

    /**
     * Information about an artifact to be retrieved from Azure Blob Storage.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * The name of the blob container.
         */
        abstract val containerName: Property<String>

        /**
         * The name of the blob within the container.
         */
        abstract val blobName: Property<String>

        /**
         * The local file to download the blob to.
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
            queue.submit(DownloadBlobAction::class.java) {
                service.set(this@AbstractBatchDownloadFromAzureBlobStorage.service)
                clientName.set(this@AbstractBatchDownloadFromAzureBlobStorage.clientName)
                containerName.set(artifact.containerName)
                blobName.set(artifact.blobName)
                outputFile.set(artifact.outputFile)
            }
        }
    }
}
