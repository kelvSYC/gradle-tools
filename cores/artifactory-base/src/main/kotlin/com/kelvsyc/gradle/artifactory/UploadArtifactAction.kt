package com.kelvsyc.gradle.artifactory

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Gradle [WorkAction] for uploading a single artifact to an Artifactory repository.
 *
 * This action is intended for use with non-Maven, non-Ivy Artifactory repositories (e.g. generic repositories).
 * For Maven or Ivy repositories, prefer Gradle's built-in publishing mechanisms (e.g. `maven-publish`) instead.
 */
abstract class UploadArtifactAction : WorkAction<UploadArtifactAction.Parameters> {
    interface Parameters : WorkParameters {
        @get:Internal
        val service: Property<ArtifactoryClientBuildService>

        val repository: Property<String>
        val path: Property<String>

        val inputFile: RegularFileProperty
    }

    override fun execute() {
        parameters.service.get().getClient()
            .repository(parameters.repository.get())
            .upload(parameters.path.get(), parameters.inputFile.get().asFile)
            .doUpload()
    }
}
