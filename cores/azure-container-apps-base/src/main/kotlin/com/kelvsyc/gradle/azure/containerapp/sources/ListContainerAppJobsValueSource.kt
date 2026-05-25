package com.kelvsyc.gradle.azure.containerapp.sources

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the list of container app job names in a resource group.
 *
 * Returns a [List] of job names (e.g., `["job-one", "job-two"]`), or an empty list
 * if no jobs are found.
 */
abstract class ListContainerAppJobsValueSource :
    ValueSource<List<String>, ListContainerAppJobsValueSource.Parameters> {

    /**
     * Parameters for [ListContainerAppJobsValueSource].
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
        return svc.getClient().jobs().listByResourceGroup(rg).map { it.name() }
    }
}
