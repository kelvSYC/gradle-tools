package com.kelvsyc.gradle.azure.appconfiguration

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that deletes an Azure App Configuration store from the configured resource group.
 */
abstract class DeleteConfigurationStoreAction : WorkAction<DeleteConfigurationStoreAction.Parameters> {
    /**
     * Parameters for [DeleteConfigurationStoreAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration manager. */
        @get:Internal
        val service: Property<AppConfigurationManagerBuildService>

        /** The name of the App Configuration store to delete. */
        val storeName: Property<String>
    }

    override fun execute() {
        val svc = parameters.service.get()
        val rg = svc.parameters.resourceGroup.get()
        svc.getClient().configurationStores()
            .deleteByResourceGroup(rg, parameters.storeName.get())
    }
}

