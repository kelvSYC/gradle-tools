package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that deletes a feature flag from Azure App Configuration.
 *
 * The feature flag is identified by its name and optional label. The internal key is constructed
 * using the [FeatureFlagConfigurationSetting.KEY_PREFIX]. If the feature flag does not exist,
 * no error is raised.
 */
abstract class DeleteFeatureFlagAction : WorkAction<DeleteFeatureFlagAction.Parameters> {
    /**
     * Parameters for [DeleteFeatureFlagAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The name of the feature flag to delete. */
        val featureName: Property<String>

        /** Optional label to identify the feature flag variant to delete. */
        val label: Property<String>
    }

    override fun execute() {
        val key = FeatureFlagConfigurationSetting.KEY_PREFIX + parameters.featureName.get()
        parameters.service.get().getClient()
            .deleteConfigurationSetting(key, parameters.label.orNull)
    }
}
