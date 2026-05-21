package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.UpdateApplicationRequest

/**
 * [WorkAction] that updates an existing AppConfig application.
 */
abstract class UpdateApplicationAction : WorkAction<UpdateApplicationAction.Parameters> {

    /**
     * Parameters for [UpdateApplicationAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application name or ID. */
        val applicationId: Property<String>

        /** Updated application name. */
        val name: Property<String>

        /** Updated application description. */
        val description: Property<String>
    }

    override fun execute() {
        val request = UpdateApplicationRequest.builder().apply {
            applicationId(parameters.applicationId.get())
            if (parameters.name.isPresent) {
                name(parameters.name.get())
            }
            if (parameters.description.isPresent) {
                description(parameters.description.get())
            }
        }.build()
        parameters.service.get().getClient().updateApplication(request)
    }
}
