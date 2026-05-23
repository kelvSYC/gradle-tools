package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.AppConfigException
import aws.sdk.kotlin.services.appconfig.startDeployment
import aws.sdk.kotlin.services.appconfig.waiters.waitUntilDeploymentComplete
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Task that starts an AppConfig deployment.
 *
 * Initiates a deployment of a configuration version to an AppConfig environment.
 * Optionally waits for the deployment to complete. When [deploymentNumberFile] is
 * configured, the deployment ID is written to the file and the task returns
 * immediately. When absent, the task waits for the deployment to reach a terminal state.
 */
@DisableCachingByDefault(because = "Communicates with AWS AppConfig; result depends on external state")
abstract class StartDeploymentTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** Application ID in AWS AppConfig. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Environment ID in AWS AppConfig. */
    @get:Input
    abstract val environmentId: Property<String>

    /** Configuration profile ID in AWS AppConfig. */
    @get:Input
    abstract val configurationProfileId: Property<String>

    /** Deployment strategy ID controlling rollout speed. */
    @get:Input
    abstract val deploymentStrategyId: Property<String>

    /** Configuration version to deploy. */
    @get:Input
    abstract val configurationVersion: Property<String>

    /** Optional file to write the deployment number to. When absent, waits for completion. */
    @get:Optional
    @get:OutputFile
    abstract val deploymentNumberFile: RegularFileProperty

    @TaskAction
    fun execute() = runBlocking {
        val client = service.get().getClient()
        val response = client.startDeployment {
            applicationId = this@StartDeploymentTask.applicationId.get()
            environmentId = this@StartDeploymentTask.environmentId.get()
            configurationProfileId = this@StartDeploymentTask.configurationProfileId.get()
            deploymentStrategyId = this@StartDeploymentTask.deploymentStrategyId.get()
            configurationVersion = this@StartDeploymentTask.configurationVersion.get()
        }
        val deploymentNum = checkNotNull(response.deploymentNumber) { "No deployment number in response" }
        if (deploymentNumberFile.isPresent) {
            deploymentNumberFile.get().asFile.writeText(deploymentNum.toString())
        } else {
            try {
                awaitDeploymentComplete(
                    client,
                    this@StartDeploymentTask.applicationId.get(),
                    this@StartDeploymentTask.environmentId.get(),
                    deploymentNum
                )
            } catch (e: AppConfigException) {
                throw GradleException("Deployment $deploymentNum failed or was rolled back", e)
            }
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
