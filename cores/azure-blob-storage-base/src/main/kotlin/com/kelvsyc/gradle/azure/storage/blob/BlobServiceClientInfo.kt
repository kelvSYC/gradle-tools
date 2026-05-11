package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient

/**
 * Configuration for a synchronous [BlobServiceClient] registration, scoped to an entire storage account.
 */
interface BlobServiceClientInfo : AzureBlobStorageClientInfo<BlobServiceClient>
