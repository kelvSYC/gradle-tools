package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.deleteEnvironment
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that deletes an AppConfig environment.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class DeleteEnvironmentTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application containing the environment. */
    @get:Input
    abstract val applicationId: Property<String>

    /** ID of the environment to delete. */
    @get:Input
    abstract val environmentId: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().deleteEnvironment {
            applicationId = this@DeleteEnvironmentTask.applicationId.get()
            environmentId = this@DeleteEnvironmentTask.environmentId.get()
        }
    }
}
