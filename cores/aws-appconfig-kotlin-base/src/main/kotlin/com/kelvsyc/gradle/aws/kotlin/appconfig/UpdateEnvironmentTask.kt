package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.updateEnvironment
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that updates an AppConfig environment.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class UpdateEnvironmentTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application containing the environment. */
    @get:Input
    abstract val applicationId: Property<String>

    /** ID of the environment to update. */
    @get:Input
    abstract val environmentId: Property<String>

    /** Optional new name for the environment. */
    @get:Optional
    @get:Input
    abstract val environmentName: Property<String>

    /** Optional new description for the environment. */
    @get:Optional
    @get:Input
    abstract val environmentDescription: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().updateEnvironment {
            applicationId = this@UpdateEnvironmentTask.applicationId.get()
            environmentId = this@UpdateEnvironmentTask.environmentId.get()
            if (environmentName.isPresent) name = environmentName.get()
            if (environmentDescription.isPresent) description = environmentDescription.get()
        }
    }
}
