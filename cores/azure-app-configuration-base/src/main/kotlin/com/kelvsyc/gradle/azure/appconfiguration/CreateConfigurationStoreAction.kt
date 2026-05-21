package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.resourcemanager.appconfiguration.models.Sku
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that creates an Azure App Configuration store in the configured resource group.
 *
 * If `sku` is absent, defaults to `Free` tier.
 */
abstract class CreateConfigurationStoreAction : WorkAction<CreateConfigurationStoreAction.Parameters> {
    /**
     * Parameters for [CreateConfigurationStoreAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration manager. */
        @get:Internal
        val service: Property<AppConfigurationManagerBuildService>

        /** The name of the App Configuration store to create. */
        val storeName: Property<String>

        /** The Azure region (location) where the store will be created. */
        val location: Property<String>

        /** The SKU tier for the store (e.g., "Free", "Standard"). Defaults to "Free" if absent. */
        val sku: Property<String>
    }

    override fun execute() {
        val svc = parameters.service.get()
        val rg = svc.parameters.resourceGroup.get()
        val skuName = if (parameters.sku.isPresent) parameters.sku.get() else "Free"
        val skuObject = Sku().withName(skuName)

        svc.getClient().configurationStores()
            .define(parameters.storeName.get())
            .withRegion(parameters.location.get())
            .withExistingResourceGroup(rg)
            .withSku(skuObject)
            .create()
    }
}

