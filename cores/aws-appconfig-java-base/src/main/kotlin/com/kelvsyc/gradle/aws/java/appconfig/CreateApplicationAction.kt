package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.CreateApplicationRequest

/**
 * [WorkAction] that creates an AppConfig application.
 */
abstract class CreateApplicationAction : WorkAction<CreateApplicationAction.Parameters> {

    /**
     * Parameters for [CreateApplicationAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** Name of the application to create. */
        val name: Property<String>

        /** Optional description of the application. */
        val description: Property<String>
    }

    override fun execute() {
        val request = CreateApplicationRequest.builder().apply {
            name(parameters.name.get())
            if (parameters.description.isPresent) {
                description(parameters.description.get())
            }
        }.build()
        parameters.service.get().getClient().createApplication(request)
    }
}
