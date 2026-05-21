package com.kelvsyc.gradle.azure.appconfiguration

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that deletes a configuration setting from Azure App Configuration.
 *
 * The setting is identified by its key and optional label. If the setting does not exist,
 * no error is raised.
 */
abstract class DeleteConfigurationSettingAction : WorkAction<DeleteConfigurationSettingAction.Parameters> {
    /**
     * Parameters for [DeleteConfigurationSettingAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The key of the configuration setting to delete. */
        val key: Property<String>

        /** Optional label to identify the configuration setting variant to delete. */
        val label: Property<String>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        client.deleteConfigurationSetting(parameters.key.get(), parameters.label.orNull)
    }
}
