package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import com.kelvsyc.gradle.azure.ResolvedAzureCredential
import org.gradle.api.provider.Property

/**
 * Build service managing an asynchronous [BlobServiceAsyncClient] scoped to an entire storage
 * account.
 *
 * See [BlobServiceClientBuildService] for the synchronous equivalent and credential configuration.
 */
abstract class BlobServiceAsyncClientBuildService :
    AbstractAzureClientBuildService<BlobServiceAsyncClient, BlobServiceAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobServiceAsyncClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** The Azure Storage account endpoint URL. */
        val endpoint: Property<String>
    }

    override fun createClient(): BlobServiceAsyncClient = BlobServiceClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        when (val credential = resolveCredential()) {
            null -> {}
            is ResolvedAzureCredential.Token -> credential(credential.credential)
            is ResolvedAzureCredential.Sas -> credential(credential.credential)
            is ResolvedAzureCredential.NamedKey -> credential(credential.credential)
        }
    }.buildAsyncClient()
}
