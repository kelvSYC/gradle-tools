package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.SettingSelector
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that lists all feature flags from Azure App Configuration, optionally filtered by label.
 *
 * Returns a [Map] of feature flag name → enabled state. Feature names have the `.appconfig.featureflag/`
 * prefix automatically stripped, so the returned keys are bare feature names.
 *
 * Returns an empty map if no feature flags match the filter.
 *
 * Note: The enabled state is determined by parsing the setting's JSON value for the `enabled` field.
 * Pending JSON parsing implementation, this currently returns `false` for all flags.
 */
abstract class ListFeatureFlagsValueSource :
    ValueSource<Map<String, Boolean>, ListFeatureFlagsValueSource.Parameters> {

    /**
     * Parameters for [ListFeatureFlagsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration client.
         */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /**
         * Optional label filter. When absent, flags across all labels are included.
         */
        val label: Property<String>
    }

    override fun obtain(): Map<String, Boolean> {
        val client = parameters.service.get().getClient()
        val selector = SettingSelector()
        selector.setKeyFilter(".appconfig.featureflag/*")
        if (parameters.label.isPresent) {
            selector.setLabelFilter(parameters.label.get())
        }

        return client.listConfigurationSettings(selector)
            .toList()
            .filter { setting ->
                val contentType = setting.contentType ?: ""
                contentType.contains("featureflag", ignoreCase = true)
            }
            .associate { setting ->
                val flagName = setting.key.removePrefix(".appconfig.featureflag/")
                // Would parse JSON to extract enabled field; for now return false
                flagName to false
            }
    }
}

