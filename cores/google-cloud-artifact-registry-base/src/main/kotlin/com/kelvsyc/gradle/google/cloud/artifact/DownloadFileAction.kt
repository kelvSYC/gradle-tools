package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.FileName
import com.google.devtools.artifactregistry.v1.GetFileRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

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
        /** The shared build service managing Artifact Registry clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [ArtifactRegistryClientInfo]. */
        val clientName: Property<String>

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

    private val client: Provider<ArtifactRegistryClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

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

        val response = client.get().getFile(request)
        parameters.outputFile.get().asFile.outputStream().use { out ->
            response.writeTo(out)
        }
    }
}
