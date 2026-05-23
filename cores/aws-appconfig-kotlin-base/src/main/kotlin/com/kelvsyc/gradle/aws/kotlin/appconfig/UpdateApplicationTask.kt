package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.updateApplication
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that updates an AppConfig application.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class UpdateApplicationTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application to update. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Optional new name for the application. */
    @get:Optional
    @get:Input
    abstract val applicationName: Property<String>

    /** Optional new description for the application. */
    @get:Optional
    @get:Input
    abstract val applicationDescription: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().updateApplication {
            applicationId = this@UpdateApplicationTask.applicationId.get()
            if (applicationName.isPresent) name = applicationName.get()
            if (applicationDescription.isPresent) description = applicationDescription.get()
        }
    }
}
