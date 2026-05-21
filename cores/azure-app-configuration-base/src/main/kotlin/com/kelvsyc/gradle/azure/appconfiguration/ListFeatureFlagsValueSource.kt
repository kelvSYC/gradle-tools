package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.SettingSelector
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.logging.Logging
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
        val selector = SettingSelector().setKeyFilter(".appconfig.featureflag/*")
        if (parameters.label.isPresent) {
            selector.setLabelFilter(parameters.label.get())
        }

        val mapper = ObjectMapper()
        return client.listConfigurationSettings(selector)
            .toList()
            .filter { setting ->
                val contentType = setting.contentType ?: ""
                contentType.contains("featureflag", ignoreCase = true)
            }
            .associate { setting ->
                val flagName = setting.key.removePrefix(".appconfig.featureflag/")
                try {
                    val json = mapper.readTree(setting.value)
                    val enabled = json.get("enabled")?.asBoolean() ?: false
                    flagName to enabled
                } catch (e: JsonProcessingException) {
                    logger.debug("Failed to parse feature flag JSON for $flagName", e)
                    flagName to false
                }
            }
    }

    private companion object {
        private val logger = Logging.getLogger(ListFeatureFlagsValueSource::class.java)
    }
}



