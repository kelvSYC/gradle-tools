package com.kelvsyc.gradle.azure.storage.blob

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that downloads a single blob from Azure Blob Storage to a local file using a synchronous
 * [BlobServiceClient][com.azure.storage.blob.BlobServiceClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution.
 */
abstract class DownloadBlobAction : WorkAction<DownloadBlobAction.Parameters> {
    /**
     * Parameters for [DownloadBlobAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the account-scoped Blob Service client. */
        @get:Internal
        val service: Property<BlobServiceClientBuildService>

        /** The name of the blob container. */
        val containerName: Property<String>

        /** The name of the blob within the container. */
        val blobName: Property<String>

        /** Destination file the blob is written to. */
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val blobClient = parameters.service.get().getClient()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())

        blobClient.downloadToFile(parameters.outputFile.get().asFile.absolutePath, true)
    }
}
