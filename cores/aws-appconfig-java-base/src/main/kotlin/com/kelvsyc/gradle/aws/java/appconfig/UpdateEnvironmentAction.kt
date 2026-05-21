package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.UpdateEnvironmentRequest

/**
 * [WorkAction] that updates an existing AppConfig environment.
 */
abstract class UpdateEnvironmentAction : WorkAction<UpdateEnvironmentAction.Parameters> {

    /**
     * Parameters for [UpdateEnvironmentAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID. */
        val applicationId: Property<String>

        /** The environment ID. */
        val environmentId: Property<String>

        /** Updated environment name. */
        val name: Property<String>

        /** Updated environment description. */
        val description: Property<String>
    }

    override fun execute() {
        val request = UpdateEnvironmentRequest.builder().apply {
            applicationId(parameters.applicationId.get())
            environmentId(parameters.environmentId.get())
            if (parameters.name.isPresent) {
                name(parameters.name.get())
            }
            if (parameters.description.isPresent) {
                description(parameters.description.get())
            }
        }.build()
        parameters.service.get().getClient().updateEnvironment(request)
    }
}
