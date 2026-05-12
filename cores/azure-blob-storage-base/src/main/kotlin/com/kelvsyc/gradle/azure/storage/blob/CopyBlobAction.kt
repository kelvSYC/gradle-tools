package com.kelvsyc.gradle.azure.storage.blob

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that copies a single blob within the same Azure Storage account using a synchronous
 * [BlobServiceClient][com.azure.storage.blob.BlobServiceClient]. Server-side copy: no data transits through
 * the build machine.
 *
 * This action uses [copyFromUrl][com.azure.storage.blob.BlobClient.copyFromUrl] which requires
 * the source blob to be accessible by the storage service (same account, public access, or SAS token).
 */
abstract class CopyBlobAction : WorkAction<CopyBlobAction.Parameters> {
    /**
     * Parameters for [CopyBlobAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the account-scoped Blob Service client. */
        val service: Property<BlobServiceClientBuildService>

        /** Source container name. */
        val sourceContainerName: Property<String>

        /** Source blob name. */
        val sourceBlobName: Property<String>

        /** Destination container name. */
        val destinationContainerName: Property<String>

        /** Destination blob name. */
        val destinationBlobName: Property<String>
    }

    override fun execute() {
        val storageClient = parameters.service.get().getClient()

        val sourceBlob = storageClient
            .getBlobContainerClient(parameters.sourceContainerName.get())
            .getBlobClient(parameters.sourceBlobName.get())

        val destinationBlob = storageClient
            .getBlobContainerClient(parameters.destinationContainerName.get())
            .getBlobClient(parameters.destinationBlobName.get())

        destinationBlob.copyFromUrl(sourceBlob.blobUrl)
    }
}
