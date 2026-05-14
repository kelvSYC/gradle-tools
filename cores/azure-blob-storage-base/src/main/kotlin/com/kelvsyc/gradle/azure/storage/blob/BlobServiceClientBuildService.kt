package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import com.kelvsyc.gradle.azure.ResolvedAzureCredential
import org.gradle.api.provider.Property

/**
 * Build service managing a synchronous [BlobServiceClient] scoped to an entire storage account.
 *
 * Configure the service at registration time using the extension functions on
 * [AzureBuildServiceParams] ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret],
 * [sasToken][com.kelvsyc.gradle.azure.sasToken],
 * [sharedKey][com.kelvsyc.gradle.azure.sharedKey], etc.) plus a per-service [Params.endpoint].
 */
abstract class BlobServiceClientBuildService :
    AbstractAzureClientBuildService<BlobServiceClient, BlobServiceClientBuildService.Params>() {
    /**
     * Configuration parameters for [BlobServiceClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** The Azure Storage account endpoint URL, e.g. `https://{accountName}.blob.core.windows.net`. */
        val endpoint: Property<String>
    }

    override fun createClient(): BlobServiceClient = BlobServiceClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        when (val credential = resolveCredential()) {
            null -> {}
            is ResolvedAzureCredential.Token -> credential(credential.credential)
            is ResolvedAzureCredential.Sas -> credential(credential.credential)
            is ResolvedAzureCredential.NamedKey -> credential(credential.credential)
        }
    }.buildClient()
}
