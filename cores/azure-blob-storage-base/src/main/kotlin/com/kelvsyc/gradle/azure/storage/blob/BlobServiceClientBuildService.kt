package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a synchronous [BlobServiceClient] scoped to an entire storage account.
 */
abstract class BlobServiceClientBuildService :
    AbstractClientBuildService<BlobServiceClient, BlobServiceClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobServiceClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /** The Azure Storage account endpoint URL, e.g. `https://{accountName}.blob.core.windows.net`. */
        val endpoint: Property<String>

        /**
         * The credential used to authenticate with Azure Blob Storage. If absent, the underlying client uses no
         * authentication.
         */
        val credential: Property<TokenCredential>
    }

    override fun createClient(): BlobServiceClient = BlobServiceClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        if (parameters.credential.isPresent) {
            credential(parameters.credential.get())
        }
    }.buildClient()
}
