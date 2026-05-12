package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a synchronous [BlobContainerClient] scoped to a single container.
 */
abstract class BlobContainerClientBuildService :
    AbstractClientBuildService<BlobContainerClient, BlobContainerClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobContainerClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The Azure Storage account endpoint URL. */
        val endpoint: Property<String>

        /** The credential used to authenticate with Azure Blob Storage. */
        val credential: Property<TokenCredential>

        /** The name of the blob container. */
        val containerName: Property<String>
    }

    override fun createClient(): BlobContainerClient = BlobContainerClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        containerName(parameters.containerName.get())
        if (parameters.credential.isPresent) {
            credential(parameters.credential.get())
        }
    }.buildClient()
}
