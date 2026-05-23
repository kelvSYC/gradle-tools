package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.AppConfigException
import aws.sdk.kotlin.services.appconfig.waiters.waitUntilDeploymentComplete
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that waits for an AppConfig deployment to reach a terminal state.
 *
 * Monitors a deployment's progress until it completes successfully or fails.
 * This task is typically used after [StartDeploymentTask] when the deployment
 * number has been written to a file and needs to be awaited separately.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class WaitForDeploymentTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** Application ID in AWS AppConfig. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Environment ID in AWS AppConfig. */
    @get:Input
    abstract val environmentId: Property<String>

    /** Deployment number to wait for. */
    @get:Input
    abstract val deploymentNumber: Property<Int>

    @TaskAction
    fun execute() = runBlocking {
        try {
            awaitDeploymentComplete(
                service.get().getClient(),
                applicationId.get(),
                environmentId.get(),
                deploymentNumber.get()
            )
        } catch (e: AppConfigException) {
            throw GradleException(
                "Deployment ${deploymentNumber.get()} failed or was rolled back",
                e
            )
        }
    }

    /**
     * Waits for the specified deployment to reach a terminal state.
     *
     * Protected to allow test overrides via mocking.
     *
     * @param client the AppConfig client
     * @param applicationId the application ID
     * @param environmentId the environment ID
     * @param deploymentNumber the deployment number to wait for
     * @throws AppConfigException if the deployment fails or is rolled back
     */
    protected open suspend fun awaitDeploymentComplete(
        client: AppConfigClient,
        applicationId: String,
        environmentId: String,
        deploymentNumber: Int,
    ) {
        client.waitUntilDeploymentComplete {
            this.applicationId = applicationId
            this.environmentId = environmentId
            this.deploymentNumber = deploymentNumber
        }
    }
}
