package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepositoryAsync
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference

/**
 * Build service managing an asynchronous [ContainerRepositoryAsync] scoped to a single
 * repository within an Azure Container Registry.
 *
 * This is a chained service — it does not manage credentials directly. Instead, it obtains
 * the repository client by calling [ContainerRepositoryAsync] on a parent async registry
 * service ([ContainerRegistryAsyncClientBuildService]). The registry service holds the
 * credentials; this service holds only the repository name.
 *
 * Register this service at the task level, providing a service reference to the async
 * registry service and the repository name:
 * ```kotlin
 * service<ContainerRepositoryAsyncClientBuildService> {
 *   registryService = containerRegistryAsyncService
 *   repositoryName = "myrepo"
 * }
 * ```
 *
 * To use this service from a [org.gradle.workers.WorkAction], declare a `Property` parameter:
 * ```kotlin
 * abstract val repositoryAsyncService: Property<ContainerRepositoryAsyncClientBuildService>
 * ```
 * and call [getClient] to access the [ContainerRepositoryAsync] instance.
 */
abstract class ContainerRepositoryAsyncClientBuildService :
    AbstractClientBuildService<ContainerRepositoryAsync, ContainerRepositoryAsyncClientBuildService.Params>() {

    /**
     * Configuration parameters for [ContainerRepositoryAsyncClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * Service reference to the parent [ContainerRegistryAsyncClientBuildService] that
         * provides the async registry connection and credentials. The registry service handles
         * all Azure authentication; this chained service uses it only to obtain the async
         * repository client.
         */
        @get:ServiceReference
        val registryService: Property<ContainerRegistryAsyncClientBuildService>

        /**
         * The name of the repository within the Azure Container Registry, e.g., `myrepo` or
         * `myorg/myrepo`. This is passed to the async registry client's getRepository method.
         */
        val repositoryName: Property<String>
    }

    override fun createClient(): ContainerRepositoryAsync =
        parameters.registryService.get().getClient().getRepository(parameters.repositoryName.get())
}
