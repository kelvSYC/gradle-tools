package com.kelvsyc.gradle.azure.storage.blob

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that deletes a single blob from Azure Blob Storage using a synchronous
 * [BlobServiceClient][com.azure.storage.blob.BlobServiceClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple blobs in a task action.
 */
abstract class DeleteBlobAction : WorkAction<DeleteBlobAction.Parameters> {
    /**
     * Parameters for [DeleteBlobAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the account-scoped Blob Service client. */
        @get:Internal
        val service: Property<BlobServiceClientBuildService>

        /** The name of the blob container. */
        val containerName: Property<String>

        /** The name of the blob to delete. */
        val blobName: Property<String>
    }

    override fun execute() {
        val blobClient = parameters.service.get().getClient()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())

        blobClient.delete()
    }
}
