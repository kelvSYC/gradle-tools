package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.stopDeployment
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that stops an in-progress AppConfig deployment.
 *
 * Cancels a deployment that has not yet reached a terminal state.
 * This is useful for rolling back a problematic deployment or for
 * cleaning up deployments that are taking too long.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class StopDeploymentTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** Application ID in AWS AppConfig. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Environment ID in AWS AppConfig. */
    @get:Input
    abstract val environmentId: Property<String>

    /** Deployment number to stop. */
    @get:Input
    abstract val deploymentNumber: Property<Int>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().stopDeployment {
            applicationId = this@StopDeploymentTask.applicationId.get()
            environmentId = this@StopDeploymentTask.environmentId.get()
            deploymentNumber = this@StopDeploymentTask.deploymentNumber.get()
        }
    }
}
