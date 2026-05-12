package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * A [WorkAction] that copies a single blob within the same Azure Storage account using a synchronous
 * [BlobServiceClient]. Server-side copy: no data transits through the build machine.
 *
 * This action uses [copyFromUrl][com.azure.storage.blob.BlobClient.copyFromUrl] which requires
 * the source blob to be accessible by the storage service (same account, public access, or SAS token).
 */
abstract class CopyBlobAction : WorkAction<CopyBlobAction.Parameters> {
    /**
     * Parameters for [CopyBlobAction].
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
         * Source container name.
         */
        val sourceContainerName: Property<String>

        /**
         * Source blob name.
         */
        val sourceBlobName: Property<String>

        /**
         * Destination container name.
         */
        val destinationContainerName: Property<String>

        /**
         * Destination blob name.
         */
        val destinationBlobName: Property<String>
    }

    private val client: Provider<BlobServiceClient> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val storageClient = client.get()

        val sourceBlob = storageClient
            .getBlobContainerClient(parameters.sourceContainerName.get())
            .getBlobClient(parameters.sourceBlobName.get())

        val destinationBlob = storageClient
            .getBlobContainerClient(parameters.destinationContainerName.get())
            .getBlobClient(parameters.destinationBlobName.get())

        destinationBlob.copyFromUrl(sourceBlob.blobUrl)
    }
}
