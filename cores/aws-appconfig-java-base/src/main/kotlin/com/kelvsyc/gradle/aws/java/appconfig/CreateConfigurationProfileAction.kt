package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.CreateConfigurationProfileRequest

/**
 * [WorkAction] that creates an AppConfig configuration profile within an application.
 *
 * Use `"hosted"` as [Parameters.locationUri] for AppConfig-hosted configurations. Set
 * [Parameters.type] to `"AWS.Freeform"` or `"AWS.AppConfig.FeatureFlags"`.
 */
abstract class CreateConfigurationProfileAction : WorkAction<CreateConfigurationProfileAction.Parameters> {

    /**
     * Parameters for [CreateConfigurationProfileAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID that will contain this profile. */
        val applicationId: Property<String>

        /** Name of the configuration profile to create. */
        val name: Property<String>

        /** URI of the configuration source. Use `"hosted"` for AppConfig-hosted configurations. */
        val locationUri: Property<String>

        /** Profile type: `"AWS.Freeform"` or `"AWS.AppConfig.FeatureFlags"`. */
        val type: Property<String>

        /** Optional description of the configuration profile. */
        val description: Property<String>
    }

    override fun execute() {
        val request = CreateConfigurationProfileRequest.builder().apply {
            applicationId(parameters.applicationId.get())
            name(parameters.name.get())
            locationUri(parameters.locationUri.get())
            type(parameters.type.get())
            if (parameters.description.isPresent) {
                description(parameters.description.get())
            }
        }.build()
        parameters.service.get().getClient().createConfigurationProfile(request)
    }
}
