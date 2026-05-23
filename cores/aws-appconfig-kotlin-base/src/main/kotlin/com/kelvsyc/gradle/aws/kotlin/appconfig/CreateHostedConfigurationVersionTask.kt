package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.createHostedConfigurationVersion
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task that creates a hosted configuration version in AWS AppConfig.
 *
 * Uploads configuration content to AppConfig as a new version and writes the assigned
 * version number to a file for use in downstream deployment tasks.
 */
abstract class CreateHostedConfigurationVersionTask : DefaultTask() {

    /** The build service managing the AppConfig client. */
    @get:Internal
    abstract val service: Property<AppConfigClientBuildService>

    /** Application ID in AWS AppConfig. */
    @get:Input
    abstract val applicationId: Property<String>

    /** Configuration profile ID in AWS AppConfig. */
    @get:Input
    abstract val configurationProfileId: Property<String>

    /** Configuration content to upload. */
    @get:Input
    abstract val content: Property<String>

    /** MIME type of the configuration content (e.g., "application/json"). */
    @get:Input
    abstract val contentType: Property<String>

    /** File to write the version number to. */
    @get:OutputFile
    abstract val versionNumberFile: RegularFileProperty

    @TaskAction
    fun execute() = runBlocking {
        val response = service.get().getClient().createHostedConfigurationVersion {
            applicationId = this@CreateHostedConfigurationVersionTask.applicationId.get()
            configurationProfileId = this@CreateHostedConfigurationVersionTask.configurationProfileId.get()
            content = this@CreateHostedConfigurationVersionTask.content.get().toByteArray()
            contentType = this@CreateHostedConfigurationVersionTask.contentType.get()
        }
        val version = checkNotNull(response.versionNumber) { "No version number in response" }
        versionNumberFile.get().asFile.writeText(version.toString())
    }
}
