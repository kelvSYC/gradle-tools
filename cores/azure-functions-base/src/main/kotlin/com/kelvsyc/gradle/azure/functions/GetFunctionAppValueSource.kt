package com.kelvsyc.gradle.azure.functions

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] providing the default hostname of a named Azure Function App, or `null` if the
 * app is not found in the configured resource group.
 */
abstract class GetFunctionAppValueSource :
    ValueSource<String, GetFunctionAppValueSource.Parameters> {

    /**
     * Parameters for [GetFunctionAppValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The ARM manager service. Resource group is read from
         * [FunctionAppClientBuildService.Params.resourceGroup].
         */
        @get:Internal
        val service: Property<FunctionAppClientBuildService>

        /** The name of the function app to look up. */
        val appName: Property<String>
    }

    override fun obtain(): String? {
        val manager = parameters.service.get().getClient()
        val resourceGroup = parameters.service.get().parameters.resourceGroup.get()
        return runCatching {
            manager.functionApps().getByResourceGroup(resourceGroup, parameters.appName.get())
                .defaultHostname()
        }.getOrNull()
    }
}
