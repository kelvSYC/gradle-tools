package com.kelvsyc.gradle.azure.appconfiguration

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that lists all Azure App Configuration stores within a resource group.
 *
 * Returns a [Map] of store name → endpoint URL. Stores with null endpoints (e.g., still
 * provisioning) are excluded from the result.
 *
 * Returns an empty map if no stores exist in the resource group.
 */
abstract class ListConfigurationStoresValueSource :
    ValueSource<Map<String, String>, ListConfigurationStoresValueSource.Parameters> {

    /**
     * Parameters for [ListConfigurationStoresValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration manager client.
         */
        @get:Internal
        val service: Property<AppConfigurationManagerBuildService>

    }

    override fun obtain(): Map<String, String> {
        val svc = parameters.service.get()
        return svc.getClient().configurationStores()
            .listByResourceGroup(svc.parameters.resourceGroup.get())
            .mapNotNull { store -> store.endpoint()?.let { store.name() to it } }
            .toMap()
    }
}
