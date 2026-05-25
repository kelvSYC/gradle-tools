package com.kelvsyc.gradle.azure.containerapp.sources

import com.azure.core.management.exception.ManagementException
import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the default domain for a managed environment.
 *
 * Returns the environment's default domain (e.g., `my-env.eastus.azurecontainerapps.io`),
 * or `null` if the environment does not exist or the domain is not available.
 */
abstract class GetManagedEnvironmentValueSource :
    ValueSource<String, GetManagedEnvironmentValueSource.Parameters> {

    /**
     * Parameters for [GetManagedEnvironmentValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Container Apps client.
         */
        @get:Internal
        val service: Property<ContainerAppsEnvironmentBuildService>
    }

    override fun obtain(): String? {
        return try {
            val svc = parameters.service.get()
            val rg = svc.parameters.resourceGroupName.get()
            val envName = svc.parameters.environmentName.get()
            svc.getClient().managedEnvironments().getByResourceGroup(rg, envName).defaultDomain()
        } catch (e: ManagementException) {
            logger.debug("Managed environment not found", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetManagedEnvironmentValueSource::class.java)
    }
}
