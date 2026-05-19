package com.kelvsyc.gradle.azure.functions

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that updates app settings on an Azure Function App via a single ARM update call.
 *
 * Plain settings ([Parameters.settings]) are serialized normally. Sensitive settings
 * ([Parameters.sensitiveSettings]) are stored as [CredentialReference] values and resolved at
 * execution time — they never appear in [WorkParameters] as plaintext. Both maps are merged and
 * applied in a single ARM operation.
 */
abstract class UpdateFunctionAppSettingsAction : WorkAction<UpdateFunctionAppSettingsAction.Parameters> {

    /**
     * Parameters for [UpdateFunctionAppSettingsAction].
     */
    interface Parameters : WorkParameters {
        /** The ARM manager service. Resource group is read from its own params. */
        @get:Internal
        val appService: Property<FunctionAppClientBuildService>

        /** The name of the function app to update. */
        val appName: Property<String>

        /** Non-sensitive app settings to apply, e.g. `LOG_LEVEL`, `REGION`. */
        val settings: MapProperty<String, String>

        /**
         * Sensitive app settings (connection strings, API keys, etc.) stored as
         * [CredentialReference] values. Resolved at execution time — not serialized as plaintext.
         */
        @get:Internal
        val sensitiveSettings: MapProperty<String, CredentialReference>
    }

    override fun execute() {
        val manager = parameters.appService.get().getClient()
        val resourceGroup = parameters.appService.get().parameters.resourceGroup.get()
        val functionApp = manager.functionApps().getByResourceGroup(resourceGroup, parameters.appName.get())

        val resolvedSensitive = parameters.sensitiveSettings.getOrElse(emptyMap())
            .mapValues { (_, ref) -> ref.resolve() }
        val allSettings = parameters.settings.getOrElse(emptyMap()) + resolvedSensitive

        functionApp.update()
            .withAppSettings(allSettings)
            .apply()
    }
}
