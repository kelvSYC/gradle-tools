package com.kelvsyc.gradle.internal.azure.storage.blob

import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.kelvsyc.gradle.azure.storage.blob.BlobServiceAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class BlobServiceAsyncClientInfoInternal :
    BlobServiceAsyncClientInfo, ServiceClientInfoInternal<BlobServiceAsyncClient> {
    override fun createClient(): BlobServiceAsyncClient {
        return BlobServiceClientBuilder().apply {
            endpoint(endpoint.get())
            if (credential.isPresent) {
                credential(credential.get())
            }
        }.buildAsyncClient()
    }
}
