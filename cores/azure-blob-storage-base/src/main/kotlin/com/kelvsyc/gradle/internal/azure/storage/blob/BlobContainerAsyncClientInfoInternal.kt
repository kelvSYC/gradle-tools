package com.kelvsyc.gradle.internal.azure.storage.blob

import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.kelvsyc.gradle.azure.storage.blob.BlobContainerAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class BlobContainerAsyncClientInfoInternal :
    BlobContainerAsyncClientInfo, ServiceClientInfoInternal<BlobContainerAsyncClient> {
    override fun createClient(): BlobContainerAsyncClient {
        return BlobContainerClientBuilder().apply {
            endpoint(endpoint.get())
            containerName(containerName.get())
            if (credential.isPresent) {
                credential(credential.get())
            }
        }.buildAsyncClient()
    }
}
