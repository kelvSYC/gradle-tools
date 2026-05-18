package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRegistryClient
import com.azure.containers.containerregistry.ContainerRegistryClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing a synchronous [ContainerRegistryClient] scoped to a single Azure
 * Container Registry instance.
 *
 * Configure the service at registration time using the extension functions on
 * [AzureBuildServiceParams] ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret], etc.) plus a per-service
 * [Params.endpoint].
 *
 * ACR accepts only [com.azure.core.credential.TokenCredential]-shaped credentials (no SAS or
 * named key support). If [AzureBuildServiceParams.credentialSource] is configured with a
 * Storage-only variant, an [IllegalArgumentException] is thrown at build service initialization.
 */
abstract class ContainerRegistryClientBuildService :
    AbstractAzureClientBuildService<ContainerRegistryClient, ContainerRegistryClientBuildService.Params>() {
    /**
     * Configuration parameters for [ContainerRegistryClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** The Azure Container Registry endpoint URL, e.g. `https://{registryName}.azurecr.io`. */
        val endpoint: Property<String>
    }

    override fun createClient(): ContainerRegistryClient = ContainerRegistryClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        resolveTokenCredential()?.let { credential(it) }
    }.buildClient()
}
