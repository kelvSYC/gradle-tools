package com.kelvsyc.gradle.azure.storage.blob

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that uploads a single local file to Azure Blob Storage using a synchronous
 * [BlobServiceClient][com.azure.storage.blob.BlobServiceClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution.
 */
abstract class UploadBlobAction : WorkAction<UploadBlobAction.Parameters> {
    /**
     * Parameters for [UploadBlobAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the account-scoped Blob Service client. */
        val service: Property<BlobServiceClientBuildService>

        /** The name of the blob container. */
        val containerName: Property<String>

        /** The name of the blob within the container. */
        val blobName: Property<String>

        /** Source file uploaded to Azure Blob Storage. */
        val inputFile: RegularFileProperty
    }

    override fun execute() {
        val blobClient = parameters.service.get().getClient()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())

        blobClient.uploadFromFile(parameters.inputFile.get().asFile.absolutePath, true)
    }
}
