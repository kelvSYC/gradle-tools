package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.util.BinaryData
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource] implementations that provide a value by reading a blob from Azure Blob Storage.
 *
 * Subclasses should implement the [doObtain] function, transforming a [BinaryData] object to an object of the
 * desired type. This class should only be used on blobs for which the entire blob can be kept in memory.
 */
abstract class AbstractBlobStorageValueSource<T : Any, P : AbstractBlobStorageValueSource.Parameters> :
    ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractBlobStorageValueSource].
     *
     * Extend this interface if there is a need to supply additional parameters to the subclass.
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the account-scoped Blob Service client. */
        @get:Internal
        val service: Property<BlobServiceClientBuildService>

        /** The name of the blob container. */
        val containerName: Property<String>

        /** The name of the blob within the container. */
        val blobName: Property<String>
    }

    /**
     * Transforms the data retrieved from Azure Blob Storage.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from Azure Blob Storage, or `null` if the data cannot be transformed.
     */
    abstract fun doObtain(content: BinaryData): T?

    override fun obtain(): T? {
        val blobClient = parameters.service.get().getClient()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())
        return doObtain(blobClient.downloadContent())
    }
}
