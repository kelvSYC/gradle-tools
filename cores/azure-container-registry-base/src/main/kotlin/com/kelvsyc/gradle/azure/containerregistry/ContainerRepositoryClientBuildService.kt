package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference

/**
 * Build service managing a synchronous [ContainerRepository] scoped to a single repository
 * within an Azure Container Registry.
 *
 * This is a chained service — it does not manage credentials directly. Instead, it obtains
 * the repository client by calling [ContainerRepository] on a parent registry service
 * ([ContainerRegistryClientBuildService]). The registry service holds the credentials;
 * this service holds only the repository name.
 *
 * Register this service at the task level, providing a service reference to the registry
 * service and the repository name:
 * ```kotlin
 * service<ContainerRepositoryClientBuildService> {
 *   registryService = containerRegistryService
 *   repositoryName = "myrepo"
 * }
 * ```
 *
 * To use this service from a [org.gradle.workers.WorkAction], declare a `Property` parameter:
 * ```kotlin
 * abstract val repositoryService: Property<ContainerRepositoryClientBuildService>
 * ```
 * and call [getClient] to access the [ContainerRepository] instance.
 */
abstract class ContainerRepositoryClientBuildService :
    AbstractClientBuildService<ContainerRepository, ContainerRepositoryClientBuildService.Params>() {

    /**
     * Configuration parameters for [ContainerRepositoryClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * Service reference to the parent [ContainerRegistryClientBuildService] that provides
         * the registry connection and credentials. The registry service handles all Azure
         * authentication; this chained service uses it only to obtain the repository client.
         */
        @get:ServiceReference
        val registryService: Property<ContainerRegistryClientBuildService>

        /**
         * The name of the repository within the Azure Container Registry, e.g., `myrepo` or
         * `myorg/myrepo`. This is passed to the registry client's getRepository method.
         */
        val repositoryName: Property<String>
    }

    override fun createClient(): ContainerRepository =
        parameters.registryService.get().getClient().getRepository(parameters.repositoryName.get())
}
