package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.StartDeploymentRequest

/**
 * [WorkAction] that starts an AppConfig deployment.
 *
 * Writes the resulting deployment number (as a plain integer string) to [Parameters.deploymentNumberFile].
 * Lifecycle plugin tasks read this file to obtain the deployment number for [WaitForDeploymentAction]
 * or [StopDeploymentAction].
 */
abstract class StartDeploymentAction : WorkAction<StartDeploymentAction.Parameters> {

    /**
     * Parameters for [StartDeploymentAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID. */
        val applicationId: Property<String>

        /** The environment ID. */
        val environmentId: Property<String>

        /** The configuration profile ID. */
        val configurationProfileId: Property<String>

        /** The deployment strategy ID. */
        val deploymentStrategyId: Property<String>

        /** The configuration version number to deploy (as a string, e.g. `"3"`). */
        val configurationVersion: Property<String>

        /** File to which the deployment number is written. */
        val deploymentNumberFile: RegularFileProperty
    }

    override fun execute() {
        val request = StartDeploymentRequest.builder()
            .applicationId(parameters.applicationId.get())
            .environmentId(parameters.environmentId.get())
            .configurationProfileId(parameters.configurationProfileId.get())
            .deploymentStrategyId(parameters.deploymentStrategyId.get())
            .configurationVersion(parameters.configurationVersion.get())
            .build()
        val response = parameters.service.get().getClient().startDeployment(request)
        parameters.deploymentNumberFile.get().asFile.writeText(response.deploymentNumber().toString())
    }
}
