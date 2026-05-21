package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import com.azure.data.appconfiguration.models.SettingSelector
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that lists all feature flags from Azure App Configuration, optionally filtered
 * by label.
 *
 * Returns a [Map] of feature flag name → enabled state. The [FeatureFlagConfigurationSetting.KEY_PREFIX]
 * (`.appconfig.featureflag/`) is automatically stripped from each returned key so that callers
 * receive bare feature names.
 *
 * Returns an empty map if no feature flags match the filter.
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
        val selector = SettingSelector().setKeyFilter(FeatureFlagConfigurationSetting.KEY_PREFIX + "*")
        if (parameters.label.isPresent) {
            selector.setLabelFilter(parameters.label.get())
        }
        return client.listConfigurationSettings(selector)
            .toList()
            .filterIsInstance<FeatureFlagConfigurationSetting>()
            .associate { it.key.removePrefix(FeatureFlagConfigurationSetting.KEY_PREFIX) to it.isEnabled }
    }
}
