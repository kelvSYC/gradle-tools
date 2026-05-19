package com.kelvsyc.gradle.azure.functions

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] providing a map of function names to their invoke URL templates for all functions
 * in a named Azure Function App.
 *
 * The URL templates may contain a `{code}` placeholder but never contain a resolved function key —
 * safe for the Gradle configuration cache.
 */
abstract class ListFunctionsValueSource :
    ValueSource<Map<String, String>, ListFunctionsValueSource.Parameters> {

    /**
     * Parameters for [ListFunctionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The ARM manager service. Resource group is read from
         * [FunctionAppClientBuildService.Params.resourceGroup].
         */
        @get:Internal
        val service: Property<FunctionAppClientBuildService>

        /** The name of the function app to list functions from. */
        val appName: Property<String>
    }

    override fun obtain(): Map<String, String> {
        val manager = parameters.service.get().getClient()
        val resourceGroup = parameters.service.get().parameters.resourceGroup.get()
        val functionApp = manager.functionApps().getByResourceGroup(resourceGroup, parameters.appName.get())
        return functionApp.listFunctions().associate { envelope ->
            val name = envelope.innerModel().id().orEmpty().substringAfterLast('/')
            val url = envelope.innerModel().invokeUrlTemplate().orEmpty()
            name to url
        }
    }
}
