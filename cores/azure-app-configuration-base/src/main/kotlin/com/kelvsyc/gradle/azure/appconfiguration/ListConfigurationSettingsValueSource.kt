package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.SettingSelector
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that lists all configuration settings from Azure App Configuration,
 * optionally filtered by key prefix and label.
 *
 * Returns a [Map] of configuration key → value. Key Vault reference entries
 * (identified by content type `application/vnd.microsoft.appconfig.keyvaultref+json`)
 * are excluded from the result. To resolve Key Vault references, use `azure-key-vault-base` separately.
 *
 * Returns an empty map if no settings match the filters.
 */
abstract class ListConfigurationSettingsValueSource :
    ValueSource<Map<String, String>, ListConfigurationSettingsValueSource.Parameters> {

    /**
     * Parameters for [ListConfigurationSettingsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration client.
         */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /**
         * Optional key filter (prefix or pattern). When absent, all keys are included.
         */
        val keyFilter: Property<String>

        /**
         * Optional label filter. When absent, settings across all labels are included.
         */
        val labelFilter: Property<String>
    }

    override fun obtain(): Map<String, String> {
        val client = parameters.service.get().getClient()
        val selector = SettingSelector()
        if (parameters.keyFilter.isPresent) {
            selector.setKeyFilter(parameters.keyFilter.get())
        }
        if (parameters.labelFilter.isPresent) {
            selector.setLabelFilter(parameters.labelFilter.get())
        }

        return client.listConfigurationSettings(selector)
            .toList()
            .filterNot { setting ->
                val contentType = setting.contentType ?: ""
                contentType.contains("keyvaultref", ignoreCase = true)
            }
            .associate { it.key to it.value }
    }
}

