package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.appconfig.model.CreateHostedConfigurationVersionRequest

/**
 * [WorkAction] that creates a new hosted configuration version in AppConfig.
 *
 * Writes the resulting version number (as a plain integer string) to [Parameters.versionNumberFile].
 * Lifecycle plugin tasks read this file to obtain the version number for [StartDeploymentAction].
 */
abstract class CreateHostedConfigurationVersionAction :
    WorkAction<CreateHostedConfigurationVersionAction.Parameters> {

    /**
     * Parameters for [CreateHostedConfigurationVersionAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application ID. */
        val applicationId: Property<String>

        /** The configuration profile ID. */
        val configurationProfileId: Property<String>

        /** Configuration content as a string (e.g. JSON or YAML). */
        val content: Property<String>

        /** MIME type of the configuration content (e.g. `"application/json"`). */
        val contentType: Property<String>

        /** File to which the created version number is written. */
        val versionNumberFile: RegularFileProperty
    }

    override fun execute() {
        val request = CreateHostedConfigurationVersionRequest.builder()
            .applicationId(parameters.applicationId.get())
            .configurationProfileId(parameters.configurationProfileId.get())
            .contentType(parameters.contentType.get())
            .content(SdkBytes.fromByteArray(parameters.content.get().toByteArray()))
            .build()
        val response = parameters.service.get().getClient()
            .createHostedConfigurationVersion(request)
        parameters.versionNumberFile.get().asFile.writeText(response.versionNumber().toString())
    }
}
