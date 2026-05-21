package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves a single configuration setting from Azure App Configuration.
 *
 * Returns the setting's value as a string. When the setting is a Key Vault reference
 * (identified by content type `application/vnd.microsoft.appconfig.keyvaultref+json`),
 * returns empty string instead. Callers should use `azure-key-vault-base` to resolve Key Vault
 * references separately.
 *
 * Returns empty string if the configuration setting is not found.
 */
abstract class GetConfigurationSettingValueSource :
    ValueSource<String, GetConfigurationSettingValueSource.Parameters> {

    /**
     * Parameters for [GetConfigurationSettingValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration client.
         */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /**
         * The configuration setting key to retrieve.
         */
        val key: Property<String>

        /**
         * Optional label filter. When absent, settings without a label are queried.
         */
        val label: Property<String>
    }

    override fun obtain(): String {
        return try {
            val client = parameters.service.get().getClient()
            val setting = client.getConfigurationSetting(
                parameters.key.get(),
                parameters.label.orNull
            )
            // Skip Key Vault references by checking content type.
            val contentType = setting.contentType ?: ""
            if (contentType.contains("keyvaultref", ignoreCase = true)) "" else setting.value
        } catch (e: ResourceNotFoundException) {
            logger.debug("Configuration setting not found: ${parameters.key.get()}", e)
            ""
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetConfigurationSettingValueSource::class.java)
    }
}


