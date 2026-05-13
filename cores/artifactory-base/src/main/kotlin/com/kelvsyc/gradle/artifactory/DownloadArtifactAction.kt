package com.kelvsyc.gradle.artifactory

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Gradle [WorkAction] for downloading a single artifact from an Artifactory repository.
 *
 * This action is intended for use with non-Maven, non-Ivy Artifactory repositories (e.g. generic repositories).
 * For Maven or Ivy repositories, prefer Gradle's built-in dependency resolution instead.
 */
abstract class DownloadArtifactAction : WorkAction<DownloadArtifactAction.Parameters> {
    interface Parameters : WorkParameters {
        @get:Internal
        val service: Property<ArtifactoryClientBuildService>

        val repository: Property<String>
        val path: Property<String>

        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val inputStream = parameters.service.get().getClient()
            .repository(parameters.repository.get())
            .download(parameters.path.get())
            .doDownload()

        inputStream.use { input ->
            parameters.outputFile.get().asFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
