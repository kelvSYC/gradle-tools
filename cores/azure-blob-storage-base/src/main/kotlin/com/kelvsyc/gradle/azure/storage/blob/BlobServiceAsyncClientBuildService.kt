package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an asynchronous [BlobServiceAsyncClient] scoped to an entire storage account.
 */
abstract class BlobServiceAsyncClientBuildService :
    AbstractClientBuildService<BlobServiceAsyncClient, BlobServiceAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobServiceAsyncClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The Azure Storage account endpoint URL. */
        val endpoint: Property<String>

        /** The credential used to authenticate with Azure Blob Storage. */
        val credential: Property<TokenCredential>
    }

    override fun createClient(): BlobServiceAsyncClient = BlobServiceClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        if (parameters.credential.isPresent) {
            credential(parameters.credential.get())
        }
    }.buildAsyncClient()
}
