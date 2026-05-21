package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.management.exception.ManagementException
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the endpoint URL for a single Azure App Configuration store.
 *
 * Returns the store's endpoint URL (e.g., `https://my-store.azconfig.io`), or `null` if:
 * - the store does not exist, or
 * - the store endpoint is not available (e.g., still provisioning).
 *
 * Uses the management-plane [AppConfigurationManagerBuildService] to enumerate stores within
 * the configured resource group.
 */
abstract class GetConfigurationStoreValueSource :
    ValueSource<String, GetConfigurationStoreValueSource.Parameters> {

    /**
     * Parameters for [GetConfigurationStoreValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration manager client.
         */
        @get:Internal
        val service: Property<AppConfigurationManagerBuildService>

        /**
         * The App Configuration store name.
         */
        val storeName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val svc = parameters.service.get()
            val store = svc.getClient().configurationStores()
                .getByResourceGroup(svc.parameters.resourceGroup.get(), parameters.storeName.get())
            store.endpoint()
        } catch (e: ManagementException) {
            logger.debug("Configuration store not found: ${parameters.storeName.get()}", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetConfigurationStoreValueSource::class.java)
    }
}
