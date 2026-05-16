package com.kelvsyc.gradle.nexus

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Gradle [WorkAction] for downloading a single artifact from a Nexus raw repository.
 *
 * The response body is streamed directly to [Parameters.outputFile], keeping heap usage constant
 * regardless of artifact size.
 */
abstract class DownloadArtifactAction : WorkAction<DownloadArtifactAction.Parameters> {

    /**
     * Parameters for [DownloadArtifactAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Nexus client.
         */
        @get:Internal
        val service: Property<NexusClientBuildService>

        /**
         * The name of the Nexus repository containing the artifact.
         */
        val repository: Property<String>

        /**
         * The path to the artifact within the repository (e.g. `com/example/1.0/artifact-1.0.jar`).
         */
        val path: Property<String>

        /**
         * The local file to which the artifact will be written.
         */
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val response = parameters.service.get().getClient()
            .downloadAsset(parameters.repository.get(), parameters.path.get())
            .execute()
        response.body()?.byteStream()?.use { input ->
            val outputFile = parameters.outputFile.get().asFile
            outputFile.parentFile.mkdirs()
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
