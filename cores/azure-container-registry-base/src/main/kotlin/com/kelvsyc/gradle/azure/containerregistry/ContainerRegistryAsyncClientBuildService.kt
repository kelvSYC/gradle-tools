package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRegistryAsyncClient
import com.azure.containers.containerregistry.ContainerRegistryClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing an asynchronous [ContainerRegistryAsyncClient] scoped to a single Azure
 * Container Registry instance.
 *
 * See [ContainerRegistryClientBuildService] for the synchronous equivalent and credential
 * configuration details.
 */
abstract class ContainerRegistryAsyncClientBuildService :
    AbstractAzureClientBuildService<ContainerRegistryAsyncClient, ContainerRegistryAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [ContainerRegistryAsyncClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** The Azure Container Registry endpoint URL, e.g. `https://{registryName}.azurecr.io`. */
        val endpoint: Property<String>
    }

    override fun createClient(): ContainerRegistryAsyncClient = ContainerRegistryClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        resolveTokenCredential()?.let { credential(it) }
    }.buildAsyncClient()
}
