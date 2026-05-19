package com.kelvsyc.gradle.azure.functions

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] providing a map of Azure Function App names to their default hostnames for all
 * apps in the resource group configured on the parent [FunctionAppClientBuildService].
 *
 * Returns an empty map when the resource group contains no function apps.
 * The result is safe to serialize to the Gradle configuration cache — no secrets are included.
 */
abstract class ListFunctionAppsValueSource :
    ValueSource<Map<String, String>, ListFunctionAppsValueSource.Parameters> {

    /**
     * Parameters for [ListFunctionAppsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The ARM manager service. Resource group is read from
         * [FunctionAppClientBuildService.Params.resourceGroup].
         */
        @get:Internal
        val service: Property<FunctionAppClientBuildService>
    }

    override fun obtain(): Map<String, String> {
        val manager = parameters.service.get().getClient()
        val resourceGroup = parameters.service.get().parameters.resourceGroup.get()
        return manager.functionApps().listByResourceGroup(resourceGroup)
            .associate { it.name() to it.defaultHostname() }
    }
}
