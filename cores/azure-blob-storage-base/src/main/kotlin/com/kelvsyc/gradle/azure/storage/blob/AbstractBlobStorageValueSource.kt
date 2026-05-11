package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Base class for [ValueSource] implementations that provide a value by reading a blob from Azure Blob Storage.
 *
 * Subclasses should implement the [doObtain] function, transforming a [BinaryData] object to an object of the
 * desired type.
 * This class should only be used on blobs for which the entire blob can be kept in memory.
 */
abstract class AbstractBlobStorageValueSource<T : Any, P : AbstractBlobStorageValueSource.Parameters> :
    ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractBlobStorageValueSource]. This contains the data needed to retrieve a blob
     * from Azure Blob Storage.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractBlobStorageValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
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
    }

    private val client: Provider<BlobServiceClient> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the data retrieved from Azure Blob Storage.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from Azure Blob Storage, or `null` if the data cannot be transformed into the
     * needed data.
     */
    abstract fun doObtain(content: BinaryData): T?

    override fun obtain(): T? {
        val blobClient = client.get()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())
        val content = blobClient.downloadContent()
        return doObtain(content)
    }
}
