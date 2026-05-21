package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.CreateEnvironmentRequest

/**
 * [WorkAction] that creates an AppConfig environment within an application.
 */
abstract class CreateEnvironmentAction : WorkAction<CreateEnvironmentAction.Parameters> {

    /**
     * Parameters for [CreateEnvironmentAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID that will contain this environment. */
        val applicationId: Property<String>

        /** Name of the environment to create. */
        val name: Property<String>

        /** Optional description of the environment. */
        val description: Property<String>
    }

    override fun execute() {
        val request = CreateEnvironmentRequest.builder().apply {
            applicationId(parameters.applicationId.get())
            name(parameters.name.get())
            if (parameters.description.isPresent) {
                description(parameters.description.get())
            }
        }.build()
        parameters.service.get().getClient().createEnvironment(request)
    }
}
