package com.kelvsyc.gradle.nexus

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Gradle [WorkAction] for uploading a single file to a Nexus raw repository.
 *
 * The caller provides a `repository` and a `path` in the form `directory/filename` (e.g.
 * `com/example/1.0/artifact-1.0.jar`). The path is split on the last `/` to produce the
 * `raw.directory` and `raw.asset1.filename` multipart fields required by the Nexus REST v1 API.
 * A path with no `/` (root-level file) sends an empty string for `raw.directory`.
 */
abstract class UploadArtifactAction : WorkAction<UploadArtifactAction.Parameters> {

    /**
     * Parameters for [UploadArtifactAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Nexus client.
         */
        @get:Internal
        val service: Property<NexusClientBuildService>

        /**
         * The name of the Nexus repository to upload to.
         */
        val repository: Property<String>

        /**
         * The target path within the repository (e.g. `com/example/1.0/artifact-1.0.jar`).
         * Split on the last `/` to derive `raw.directory` and `raw.asset1.filename`.
         */
        val path: Property<String>

        /**
         * The local file to upload.
         */
        val inputFile: RegularFileProperty
    }

    override fun execute() {
        val path = parameters.path.get()
        val lastSlash = path.lastIndexOf('/')
        val directory = if (lastSlash >= 0) path.substring(0, lastSlash) else ""
        val filename = if (lastSlash >= 0) path.substring(lastSlash + 1) else path

        val textMediaType = "text/plain".toMediaType()
        val file = parameters.inputFile.get().asFile
        val rawPart = MultipartBody.Part.createFormData(
            "raw.asset1",
            filename,
            file.asRequestBody("application/octet-stream".toMediaType()),
        )

        parameters.service.get().getClient()
            .uploadRawAsset(
                parameters.repository.get(),
                directory.toRequestBody(textMediaType),
                filename.toRequestBody(textMediaType),
                rawPart,
            )
            .execute()
    }
}
