package com.kelvsyc.gradle.aws.kotlin.appconfig

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task that downloads the current AppConfig configuration to a local file.
 *
 * Retrieves the deployed configuration for the specified application, environment,
 * and configuration profile, writing it to the task's output file. Uses the
 * [AppConfigDataClientBuildService] which manages the AppConfig Data session protocol.
 */
abstract class DownloadConfigurationTask : DefaultTask() {

    /** The build service managing the AppConfig Data client. */
    @get:Internal
    abstract val service: Property<AppConfigDataClientBuildService>

    /** Application identifier in AWS AppConfig. */
    @get:Input
    abstract val applicationIdentifier: Property<String>

    /** Environment identifier in AWS AppConfig. */
    @get:Input
    abstract val environmentIdentifier: Property<String>

    /** Configuration profile identifier in AWS AppConfig. */
    @get:Input
    abstract val configurationProfileIdentifier: Property<String>

    /** File to write the configuration content to. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() = runBlocking {
        val bytes = service.get().fetchConfiguration(
            applicationIdentifier.get(),
            environmentIdentifier.get(),
            configurationProfileIdentifier.get(),
        ) ?: throw GradleException(
            "Failed to fetch configuration for " +
                "${applicationIdentifier.get()}/${environmentIdentifier.get()}/" +
                configurationProfileIdentifier.get()
        )
        outputFile.get().asFile.writeBytes(bytes)
    }
}
