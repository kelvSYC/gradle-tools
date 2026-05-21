package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the enabled state of a feature flag from Azure App Configuration.
 *
 * Feature flag keys in Azure App Configuration are stored with the prefix
 * [FeatureFlagConfigurationSetting.KEY_PREFIX] (`.appconfig.featureflag/`). Callers provide
 * only the feature name; this class constructs the full key internally.
 *
 * Returns `true` if the feature flag exists and is enabled, `false` if it exists and is disabled,
 * or `null` if the feature flag is not found.
 */
abstract class GetFeatureFlagValueSource :
    ValueSource<Boolean, GetFeatureFlagValueSource.Parameters> {

    /**
     * Parameters for [GetFeatureFlagValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration client.
         */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /**
         * The feature flag name (without the [FeatureFlagConfigurationSetting.KEY_PREFIX]).
         */
        val featureName: Property<String>

        /**
         * Optional label filter. When absent, the flag without a label is queried.
         */
        val label: Property<String>
    }

    override fun obtain(): Boolean? {
        return try {
            val client = parameters.service.get().getClient()
            val key = FeatureFlagConfigurationSetting.KEY_PREFIX + parameters.featureName.get()
            val setting = client.getConfigurationSetting(key, parameters.label.orNull)
            if (setting is FeatureFlagConfigurationSetting) setting.isEnabled else null
        } catch (e: ResourceNotFoundException) {
            logger.debug("Feature flag not found: ${parameters.featureName.get()}", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetFeatureFlagValueSource::class.java)
    }
}
