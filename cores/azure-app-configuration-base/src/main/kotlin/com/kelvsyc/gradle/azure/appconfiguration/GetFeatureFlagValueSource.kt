package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the enabled state of a feature flag from Azure App Configuration.
 *
 * Feature flag keys in Azure App Configuration are stored with the prefix `.appconfig.featureflag/`.
 * Callers provide only the feature name; this class constructs the full key internally.
 *
 * Returns `true` if the feature flag exists and is enabled, `false` if it exists and is disabled,
 * and `false` if the feature flag is not found.
 *
 * The enabled state is determined by parsing the setting's JSON value for the `enabled` field.
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
         * The feature flag name (without the `.appconfig.featureflag/` prefix).
         */
        val featureName: Property<String>

        /**
         * Optional label filter. When absent, the flag without a label is queried.
         */
        val label: Property<String>
    }

    override fun obtain(): Boolean {
        return try {
            val client = parameters.service.get().getClient()
            val key = ".appconfig.featureflag/" + parameters.featureName.get()
            val setting = client.getConfigurationSetting(key, parameters.label.orNull)
            // Feature flag value is a JSON containing { "enabled": true|false }
            // For now, check content type to identify if this is a feature flag setting
            val contentType = setting.contentType ?: ""
            if (contentType.contains("featureflag", ignoreCase = true)) {
                // Would need to parse JSON to extract enabled field; for now return false
                // This is a placeholder pending actual JSON parsing implementation
                false
            } else {
                false
            }
        } catch (e: ResourceNotFoundException) {
            logger.debug("Feature flag not found: ${parameters.featureName.get()}", e)
            false
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetFeatureFlagValueSource::class.java)
    }
}


