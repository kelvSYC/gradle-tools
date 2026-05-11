package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobContainerAsyncClient
import org.gradle.api.provider.Property

/**
 * Configuration for an asynchronous [BlobContainerAsyncClient] registration, scoped to a single container.
 */
interface BlobContainerAsyncClientInfo : AzureBlobStorageClientInfo<BlobContainerAsyncClient> {
    /**
     * The name of the blob container.
     */
    val containerName: Property<String>
}
