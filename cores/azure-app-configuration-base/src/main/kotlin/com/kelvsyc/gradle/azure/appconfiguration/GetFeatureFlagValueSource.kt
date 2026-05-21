package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
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
 * Returns `true` if the feature flag exists and is enabled, and `false` if it is disabled or not found.
 *
 * The enabled state is extracted from the setting's JSON value by reading the `enabled` field.
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
            val contentType = setting.contentType ?: ""
            if (contentType.contains("featureflag", ignoreCase = true)) {
                // Feature flag value is a JSON containing { "enabled": true|false }
                try {
                    val mapper = ObjectMapper()
                    val json = mapper.readTree(setting.value)
                    json.get("enabled")?.asBoolean() ?: false
                } catch (e: JsonProcessingException) {
                    logger.debug("Failed to parse feature flag JSON for ${parameters.featureName.get()}", e)
                    false
                }
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




