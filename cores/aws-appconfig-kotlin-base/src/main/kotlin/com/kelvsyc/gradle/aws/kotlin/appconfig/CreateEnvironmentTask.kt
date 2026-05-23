package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.createEnvironment
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that creates an AppConfig environment within an application.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class CreateEnvironmentTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application to create the environment in. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Name of the environment to create. */
    @get:Input
    abstract val environmentName: Property<String>

    /** Optional description of the environment. */
    @get:Optional
    @get:Input
    abstract val environmentDescription: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().createEnvironment {
            applicationId = this@CreateEnvironmentTask.applicationId.get()
            name = environmentName.get()
            if (environmentDescription.isPresent) description = environmentDescription.get()
        }
    }
}
