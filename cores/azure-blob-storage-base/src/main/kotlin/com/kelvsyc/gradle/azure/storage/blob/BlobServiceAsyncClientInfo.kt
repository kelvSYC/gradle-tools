package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceAsyncClient

/**
 * Configuration for an asynchronous [BlobServiceAsyncClient] registration, scoped to an entire storage account.
 */
interface BlobServiceAsyncClientInfo : AzureBlobStorageClientInfo<BlobServiceAsyncClient>
