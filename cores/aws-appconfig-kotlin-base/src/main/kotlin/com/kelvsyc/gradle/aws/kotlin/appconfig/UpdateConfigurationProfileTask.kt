package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.updateConfigurationProfile
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that updates an AppConfig configuration profile.
 */
@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
abstract class UpdateConfigurationProfileTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** ID of the application containing the configuration profile. */
    @get:Input
    abstract val applicationId: Property<String>

    /** ID of the configuration profile to update. */
    @get:Input
    abstract val configurationProfileId: Property<String>

    /** Optional new name for the configuration profile. */
    @get:Optional
    @get:Input
    abstract val profileName: Property<String>

    /** Optional new description for the configuration profile. */
    @get:Optional
    @get:Input
    abstract val profileDescription: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        service.get().getClient().updateConfigurationProfile {
            applicationId = this@UpdateConfigurationProfileTask.applicationId.get()
            configurationProfileId = this@UpdateConfigurationProfileTask.configurationProfileId.get()
            if (profileName.isPresent) name = profileName.get()
            if (profileDescription.isPresent) description = profileDescription.get()
        }
    }
}
