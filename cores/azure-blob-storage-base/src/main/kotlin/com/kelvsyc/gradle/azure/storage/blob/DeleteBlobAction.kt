package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * A [WorkAction] that deletes a single blob from Azure Blob Storage using a synchronous [BlobServiceClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple blobs in a task action.
 */
abstract class DeleteBlobAction : WorkAction<DeleteBlobAction.Parameters> {
    /**
     * Parameters for [DeleteBlobAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered Azure Blob Storage client.
         */
        @get:Internal
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
         * The name of the blob to delete.
         */
        val blobName: Property<String>
    }

    private val client: Provider<BlobServiceClient> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val blobClient = client.get()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())

        blobClient.delete()
    }
}
