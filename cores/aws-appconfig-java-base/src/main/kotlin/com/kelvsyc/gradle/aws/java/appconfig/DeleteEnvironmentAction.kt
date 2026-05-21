package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.DeleteEnvironmentRequest

/**
 * [WorkAction] that deletes an AppConfig environment.
 */
abstract class DeleteEnvironmentAction : WorkAction<DeleteEnvironmentAction.Parameters> {

    /**
     * Parameters for [DeleteEnvironmentAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID. */
        val applicationId: Property<String>

        /** The environment ID. */
        val environmentId: Property<String>
    }

    override fun execute() {
        val request = DeleteEnvironmentRequest.builder()
            .applicationId(parameters.applicationId.get())
            .environmentId(parameters.environmentId.get())
            .build()
        parameters.service.get().getClient().deleteEnvironment(request)
    }
}
