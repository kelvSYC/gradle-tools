package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Base class for [ValueSource] implementations that produce a value by listing blobs in an Azure Blob Storage
 * container.
 *
 * Pagination is handled internally; subclasses receive the full list of [BlobItem] entries across all pages
 * via [doObtain] and transform it to the desired type.
 */
abstract class AbstractListBlobsValueSource<T : Any, P : AbstractListBlobsValueSource.Parameters> :
    ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractListBlobsValueSource].
     *
     * Extend this interface if there is a need to supply additional parameters to the subclass.
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
         * Optional blob name prefix used to filter the listing.
         */
        val prefix: Property<String>
    }

    private val client: Provider<BlobServiceClient> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the listed blobs into the target type.
     *
     * @param blobs All [BlobItem] entries returned across paginated responses.
     * @return The transformed value, or `null` if the listing cannot be transformed.
     */
    abstract fun doObtain(blobs: List<BlobItem>): T?

    override fun obtain(): T? {
        val options = ListBlobsOptions().apply {
            parameters.prefix.orNull?.let { setPrefix(it) }
        }

        val blobs = client.get()
            .getBlobContainerClient(parameters.containerName.get())
            .listBlobs(options, null)
            .toList()
        return doObtain(blobs)
    }
}
