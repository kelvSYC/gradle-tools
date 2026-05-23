package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.createConfigurationProfile
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that creates an AppConfig configuration profile within an application.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class CreateConfigurationProfileTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application to create the configuration profile in. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Name of the configuration profile to create. */
    @get:Input
    abstract val profileName: Property<String>

    /** URI pointing to the configuration data location. */
    @get:Input
    abstract val locationUri: Property<String>

    /** Type of configuration profile (e.g., AWS.AppConfig.FeatureFlags or AWS.Freeform). */
    @get:Input
    abstract val type: Property<String>

    /** Optional description of the configuration profile. */
    @get:Optional
    @get:Input
    abstract val profileDescription: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().createConfigurationProfile {
            applicationId = this@CreateConfigurationProfileTask.applicationId.get()
            name = profileName.get()
            locationUri = this@CreateConfigurationProfileTask.locationUri.get()
            type = this@CreateConfigurationProfileTask.type.get()
            if (profileDescription.isPresent) description = profileDescription.get()
        }
    }
}
