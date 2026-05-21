package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.ConfigurationSetting
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that sets a configuration setting in Azure App Configuration.
 *
 * If the setting already exists, it is updated. Optional properties (label and content type)
 * are only applied if present.
 */
abstract class SetConfigurationSettingAction : WorkAction<SetConfigurationSettingAction.Parameters> {
    /**
     * Parameters for [SetConfigurationSettingAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The key of the configuration setting. */
        val key: Property<String>

        /** The value of the configuration setting. */
        val value: Property<String>

        /** Optional label to identify the configuration setting (e.g., environment name). */
        val label: Property<String>

        /** Optional content type of the configuration setting (e.g., "application/json"). */
        val contentType: Property<String>
    }

    override fun execute() {
        val setting = ConfigurationSetting()
            .setKey(parameters.key.get())
            .setValue(parameters.value.get())
        if (parameters.label.isPresent) {
            setting.setLabel(parameters.label.get())
        }
        if (parameters.contentType.isPresent) {
            setting.setContentType(parameters.contentType.get())
        }
        parameters.service.get().getClient().setConfigurationSetting(setting)
    }
}
