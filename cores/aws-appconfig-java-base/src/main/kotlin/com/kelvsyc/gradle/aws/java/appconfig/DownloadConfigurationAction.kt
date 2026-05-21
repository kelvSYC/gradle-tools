package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that downloads the current deployed AppConfig configuration to a local file.
 *
 * Submit through `WorkerExecutor.noIsolation()`. Throws [GradleException] if the configuration
 * cannot be retrieved.
 */
abstract class DownloadConfigurationAction : WorkAction<DownloadConfigurationAction.Parameters> {

    /**
     * Parameters for [DownloadConfigurationAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig Data client. */
        @get:Internal
        val service: Property<AppConfigDataClientBuildService>

        /** Application name or ID. */
        val applicationIdentifier: Property<String>

        /** Environment name or ID. */
        val environmentIdentifier: Property<String>

        /** Configuration profile name or ID. */
        val configurationProfileIdentifier: Property<String>

        /** Destination file the configuration is written to. */
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val bytes = parameters.service.get().fetchConfiguration(
            parameters.applicationIdentifier.get(),
            parameters.environmentIdentifier.get(),
            parameters.configurationProfileIdentifier.get(),
        ) ?: throw GradleException(
            "Unable to fetch AppConfig configuration for " +
            "${parameters.applicationIdentifier.get()}/${parameters.environmentIdentifier.get()}/" +
            parameters.configurationProfileIdentifier.get(),
        )
        parameters.outputFile.get().asFile.writeBytes(bytes)
    }
}
