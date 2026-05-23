package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.createApplication
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that creates an AppConfig application.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class CreateApplicationTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** Name of the application to create. */
    @get:Input
    abstract val applicationName: Property<String>

    /** Optional description of the application. */
    @get:Optional
    @get:Input
    abstract val applicationDescription: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().createApplication {
            name = applicationName.get()
            if (applicationDescription.isPresent) description = applicationDescription.get()
        }
    }
}
