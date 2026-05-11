package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobContainerClient
import org.gradle.api.provider.Property

/**
 * Configuration for a synchronous [BlobContainerClient] registration, scoped to a single container.
 */
interface BlobContainerClientInfo : AzureBlobStorageClientInfo<BlobContainerClient> {
    /**
     * The name of the blob container.
     */
    val containerName: Property<String>
}
