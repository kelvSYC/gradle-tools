package com.kelvsyc.gradle.internal.azure.storage.blob

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.kelvsyc.gradle.azure.storage.blob.BlobContainerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class BlobContainerClientInfoInternal :
    BlobContainerClientInfo, ServiceClientInfoInternal<BlobContainerClient> {
    override fun createClient(): BlobContainerClient {
        return BlobContainerClientBuilder().apply {
            endpoint(endpoint.get())
            containerName(containerName.get())
            if (credential.isPresent) {
                credential(credential.get())
            }
        }.buildClient()
    }
}
