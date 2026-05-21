package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.UpdateConfigurationProfileRequest

/**
 * [WorkAction] that updates an existing AppConfig configuration profile.
 */
abstract class UpdateConfigurationProfileAction : WorkAction<UpdateConfigurationProfileAction.Parameters> {

    /**
     * Parameters for [UpdateConfigurationProfileAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID. */
        val applicationId: Property<String>

        /** The configuration profile ID. */
        val configurationProfileId: Property<String>

        /** Updated profile name. */
        val name: Property<String>

        /** Updated profile description. */
        val description: Property<String>
    }

    override fun execute() {
        val request = UpdateConfigurationProfileRequest.builder().apply {
            applicationId(parameters.applicationId.get())
            configurationProfileId(parameters.configurationProfileId.get())
            if (parameters.name.isPresent) {
                name(parameters.name.get())
            }
            if (parameters.description.isPresent) {
                description(parameters.description.get())
            }
        }.build()
        parameters.service.get().getClient().updateConfigurationProfile(request)
    }
}
