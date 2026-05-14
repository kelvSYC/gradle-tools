package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import com.kelvsyc.gradle.azure.ResolvedAzureCredential
import org.gradle.api.provider.Property

/**
 * Build service managing an asynchronous [BlobContainerAsyncClient] scoped to a single container.
 *
 * See [BlobContainerClientBuildService] for the synchronous equivalent and credential
 * configuration.
 */
abstract class BlobContainerAsyncClientBuildService :
    AbstractAzureClientBuildService<BlobContainerAsyncClient, BlobContainerAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobContainerAsyncClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** The Azure Storage account endpoint URL. */
        val endpoint: Property<String>

        /** The name of the blob container. */
        val containerName: Property<String>
    }

    override fun createClient(): BlobContainerAsyncClient = BlobContainerClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        containerName(parameters.containerName.get())
        when (val credential = resolveCredential()) {
            null -> {}
            is ResolvedAzureCredential.Token -> credential(credential.credential)
            is ResolvedAzureCredential.Sas -> credential(credential.credential)
            is ResolvedAzureCredential.NamedKey -> credential(credential.credential)
        }
    }.buildAsyncClient()
}
