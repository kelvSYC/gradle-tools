package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.GetRepositoryRequest
import com.google.devtools.artifactregistry.v1.Repository
import com.google.devtools.artifactregistry.v1.RepositoryName
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a Google Cloud Artifact Registry [Repository] resource.
 *
 * The value is obtained from a `GetRepository` call. The returned [Repository] proto exposes format, mode,
 * description, labels, and other repository metadata.
 */
abstract class GetRepositoryValueSource : ValueSource<Repository, GetRepositoryValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Artifact Registry client. */
        @get:Internal
        val service: Property<ArtifactRegistryClientBuildService>

        /** GCP project ID. */
        val projectName: Property<String>

        /** Artifact Registry location (e.g. `us-east1`). */
        val location: Property<String>

        /** Repository name. */
        val repository: Property<String>
    }

    override fun obtain(): Repository? {
        val name = RepositoryName.of(
            parameters.projectName.get(),
            parameters.location.get(),
            parameters.repository.get(),
        ).toString()
        val request = GetRepositoryRequest.newBuilder().apply {
            this.name = name
        }.build()
        return parameters.service.get().getClient().getRepository(request)
    }
}
