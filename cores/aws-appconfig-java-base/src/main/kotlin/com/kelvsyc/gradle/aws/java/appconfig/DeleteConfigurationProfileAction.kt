package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.DeleteConfigurationProfileRequest

/**
 * [WorkAction] that deletes an AppConfig configuration profile.
 */
abstract class DeleteConfigurationProfileAction : WorkAction<DeleteConfigurationProfileAction.Parameters> {

    /**
     * Parameters for [DeleteConfigurationProfileAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID. */
        val applicationId: Property<String>

        /** The configuration profile ID. */
        val configurationProfileId: Property<String>
    }

    override fun execute() {
        val request = DeleteConfigurationProfileRequest.builder()
            .applicationId(parameters.applicationId.get())
            .configurationProfileId(parameters.configurationProfileId.get())
            .build()
        parameters.service.get().getClient().deleteConfigurationProfile(request)
    }
}
