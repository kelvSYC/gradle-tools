package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.deleteConfigurationProfile
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that deletes an AppConfig configuration profile.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class DeleteConfigurationProfileTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application containing the configuration profile. */
    @get:Input
    abstract val applicationId: Property<String>

    /** ID of the configuration profile to delete. */
    @get:Input
    abstract val configurationProfileId: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().deleteConfigurationProfile {
            applicationId = this@DeleteConfigurationProfileTask.applicationId.get()
            configurationProfileId = this@DeleteConfigurationProfileTask.configurationProfileId.get()
        }
    }
}
