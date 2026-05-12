package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an asynchronous [BlobContainerAsyncClient] scoped to a single container.
 */
abstract class BlobContainerAsyncClientBuildService :
    AbstractClientBuildService<BlobContainerAsyncClient, BlobContainerAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobContainerAsyncClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The Azure Storage account endpoint URL. */
        val endpoint: Property<String>

        /** The credential used to authenticate with Azure Blob Storage. */
        val credential: Property<TokenCredential>

        /** The name of the blob container. */
        val containerName: Property<String>
    }

    override fun createClient(): BlobContainerAsyncClient = BlobContainerClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        containerName(parameters.containerName.get())
        if (parameters.credential.isPresent) {
            credential(parameters.credential.get())
        }
    }.buildAsyncClient()
}
