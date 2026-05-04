package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListPackagesRequest
import com.google.devtools.artifactregistry.v1.RepositoryName
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation providing a list of package resource names within an Artifact Registry repository.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/repositories/{repository}/packages/{package}`.
 */
abstract class ListPackagesValueSource : ValueSource<List<String>, ListPackagesValueSource.Parameters> {
    /**
     * Parameters for [ListPackagesValueSource].
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

    override fun obtain(): List<String>? {
        val parent = RepositoryName.of(
            parameters.projectName.get(),
            parameters.location.get(),
            parameters.repository.get(),
        ).toString()
        val request = ListPackagesRequest.newBuilder().apply {
            this.parent = parent
        }.build()
        return client.get().listPackages(request).iterateAll().map { it.name }
    }
}
