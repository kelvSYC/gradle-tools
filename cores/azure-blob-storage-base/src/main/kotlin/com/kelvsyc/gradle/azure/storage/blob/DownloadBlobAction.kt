package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that downloads a single blob from Azure Blob Storage to a local file using a synchronous
 * [BlobServiceClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution.
 */
abstract class DownloadBlobAction : WorkAction<DownloadBlobAction.Parameters> {
    /**
     * Parameters for [DownloadBlobAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered Azure Blob Storage client.
         */
        val service: Property<ClientsBaseService>

        /**
         * Registered name of a [BlobServiceClientInfo].
         */
        val clientName: Property<String>

        /**
         * The name of the blob container.
         */
        val containerName: Property<String>

        /**
         * The name of the blob within the container.
         */
        val blobName: Property<String>

        /**
         * Destination file the blob is written to.
         */
        val outputFile: RegularFileProperty
    }

    private val client: Provider<BlobServiceClient> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val blobClient = client.get()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())

        blobClient.downloadToFile(parameters.outputFile.get().asFile.absolutePath, true)
    }
}
