package com.kelvsyc.gradle.internal.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class BlobServiceClientInfoInternal : BlobServiceClientInfo, ServiceClientInfoInternal<BlobServiceClient> {
    override fun createClient(): BlobServiceClient {
        return BlobServiceClientBuilder().apply {
            endpoint(endpoint.get())
            if (credential.isPresent) {
                credential(credential.get())
            }
        }.buildClient()
    }
}
