package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that sets a feature flag in Azure App Configuration.
 *
 * Feature flags are created as configuration settings with the `.appconfig.featureflag/` key prefix.
 * If the feature flag already exists, it is updated. Optional properties (label and description)
 * are only applied if present.
 */
abstract class SetFeatureFlagAction : WorkAction<SetFeatureFlagAction.Parameters> {
    /**
     * Parameters for [SetFeatureFlagAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The name of the feature flag. */
        val featureName: Property<String>

        /** Whether the feature flag is enabled or disabled. */
        val enabled: Property<Boolean>

        /** Optional label to identify the feature flag variant (e.g., environment name). */
        val label: Property<String>

        /** Optional description of the feature flag's purpose. */
        val description: Property<String>
    }

    override fun execute() {
        val setting = FeatureFlagConfigurationSetting(
            parameters.featureName.get(),
            parameters.enabled.get()
        )
        if (parameters.label.isPresent) {
            setting.setLabel(parameters.label.get())
        }
        if (parameters.description.isPresent) {
            setting.setDescription(parameters.description.get())
        }
        parameters.service.get().getClient().setConfigurationSetting(setting)
    }
}
