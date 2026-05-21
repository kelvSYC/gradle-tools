package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.DeleteApplicationRequest

/**
 * [WorkAction] that deletes an AppConfig application.
 */
abstract class DeleteApplicationAction : WorkAction<DeleteApplicationAction.Parameters> {

    /**
     * Parameters for [DeleteApplicationAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application name or ID. */
        val applicationId: Property<String>
    }

    override fun execute() {
        val request = DeleteApplicationRequest.builder()
            .applicationId(parameters.applicationId.get())
            .build()
        parameters.service.get().getClient().deleteApplication(request)
    }
}
