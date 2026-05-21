package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.StopDeploymentRequest

/**
 * [WorkAction] that stops an in-progress AppConfig deployment, triggering a rollback.
 */
abstract class StopDeploymentAction : WorkAction<StopDeploymentAction.Parameters> {

    /**
     * Parameters for [StopDeploymentAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application name or ID. */
        val applicationId: Property<String>

        /** The environment name or ID. */
        val environmentId: Property<String>

        /** The deployment number to stop. */
        val deploymentNumber: Property<Int>
    }

    override fun execute() {
        val request = StopDeploymentRequest.builder()
            .applicationId(parameters.applicationId.get())
            .environmentId(parameters.environmentId.get())
            .deploymentNumber(parameters.deploymentNumber.get())
            .build()
        parameters.service.get().getClient().stopDeployment(request)
    }
}
