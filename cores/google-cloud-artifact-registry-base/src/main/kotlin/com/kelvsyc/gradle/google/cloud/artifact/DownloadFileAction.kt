package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.FileName
import com.google.devtools.artifactregistry.v1.GetFileRequest
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation downloading a file from Google Cloud Artifact Registry to a local file.
 *
 * Mirrors the behaviour of [AbstractArtifactValueSource] but writes to a [RegularFileProperty] instead
 * of streaming into memory, providing parity with the action-style download primitives in the AWS bases.
 */
abstract class DownloadFileAction : WorkAction<DownloadFileAction.Parameters> {
    /**
     * Parameters for [DownloadFileAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Artifact Registry client. */
        @get:Internal
        val service: Property<ArtifactRegistryClientBuildService>

        /** GCP project ID. */
        val projectName: Property<String>

        /** Artifact Registry location (e.g. `us-east1`). */
        val location: Property<String>

        /** Repository name. */
        val repository: Property<String>

        /** File path within the repository. */
        val filename: Property<String>

        /** The destination file the response will be written to. */
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val fileInternal = FileName.newBuilder().apply {
            project = parameters.projectName.get()
            location = parameters.location.get()
            repository = parameters.repository.get()
            file = parameters.filename.get()
        }.build()

        val request = GetFileRequest.newBuilder().apply {
            name = fileInternal.toString()
        }.build()

        val response = parameters.service.get().getClient().getFile(request)
        parameters.outputFile.get().asFile.outputStream().use { out ->
            response.writeTo(out)
        }
    }
}
