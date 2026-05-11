package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that uploads a single local file to Azure Blob Storage using a synchronous [BlobServiceClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution.
 */
abstract class UploadBlobAction : WorkAction<UploadBlobAction.Parameters> {
    /**
     * Parameters for [UploadBlobAction].
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
         * Source file uploaded to Azure Blob Storage.
         */
        val inputFile: RegularFileProperty
    }

    private val client: Provider<BlobServiceClient> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val blobClient = client.get()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())

        blobClient.uploadFromFile(parameters.inputFile.get().asFile.absolutePath, true)
    }
}
