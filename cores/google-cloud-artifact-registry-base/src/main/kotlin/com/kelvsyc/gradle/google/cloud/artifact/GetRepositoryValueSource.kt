package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.GetRepositoryRequest
import com.google.devtools.artifactregistry.v1.Repository
import com.google.devtools.artifactregistry.v1.RepositoryName
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

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
        /** The shared build service managing Artifact Registry clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of an [ArtifactRegistryClientInfo]. */
        val clientName: Property<String>

        /** GCP project ID. */
        val projectName: Property<String>

        /** Artifact Registry location (e.g. `us-east1`). */
        val location: Property<String>

        /** Repository name. */
        val repository: Property<String>
    }

    private val client: Provider<ArtifactRegistryClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Repository? {
        val name = RepositoryName.of(
            parameters.projectName.get(),
            parameters.location.get(),
            parameters.repository.get(),
        ).toString()
        val request = GetRepositoryRequest.newBuilder().apply {
            this.name = name
        }.build()
        return client.get().getRepository(request)
    }
}
