package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import com.kelvsyc.gradle.azure.ResolvedAzureCredential
import org.gradle.api.provider.Property

/**
 * Build service managing a synchronous [BlobContainerClient] scoped to a single container.
 *
 * See [BlobServiceClientBuildService] for credential configuration; [Params.containerName]
 * additionally identifies the target container.
 */
abstract class BlobContainerClientBuildService :
    AbstractAzureClientBuildService<BlobContainerClient, BlobContainerClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobContainerClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** The Azure Storage account endpoint URL. */
        val endpoint: Property<String>

        /** The name of the blob container. */
        val containerName: Property<String>
    }

    override fun createClient(): BlobContainerClient = BlobContainerClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        containerName(parameters.containerName.get())
        when (val credential = resolveCredential()) {
            null -> {}
            is ResolvedAzureCredential.Token -> credential(credential.credential)
            is ResolvedAzureCredential.Sas -> credential(credential.credential)
            is ResolvedAzureCredential.NamedKey -> credential(credential.credential)
        }
    }.buildClient()
}
