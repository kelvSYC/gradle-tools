package com.kelvsyc.gradle.azure.containerapp.sources

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the list of container app names in a managed environment.
 *
 * Returns a [List] of app names (e.g., `["app-one", "app-two"]`), or an empty list if
 * no apps are found.
 */
abstract class ListContainerAppsValueSource :
    ValueSource<List<String>, ListContainerAppsValueSource.Parameters> {

    /**
     * Parameters for [ListContainerAppsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Container Apps client.
         */
        @get:Internal
        val service: Property<ContainerAppsEnvironmentBuildService>
    }

    override fun obtain(): List<String> {
        val svc = parameters.service.get()
        val rg = svc.parameters.resourceGroupName.get()
        return svc.getClient().containerApps().listByResourceGroup(rg).map { it.name() }
    }
}
