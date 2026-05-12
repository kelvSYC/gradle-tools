package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListFilesRequest
import com.google.devtools.artifactregistry.v1.RepositoryName
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of file resource names within an Artifact Registry repository.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/repositories/{repository}/files/{file}`.
 *
 * An optional [Parameters.filter] may be supplied to narrow the result. See the Artifact Registry
 * `ListFiles` documentation for supported filter expressions (e.g. `owner="…/packages/foo/versions/1.0.0"`).
 */
abstract class ListFilesValueSource : ValueSource<List<String>, ListFilesValueSource.Parameters> {
    /**
     * Parameters for [ListFilesValueSource].
     */
    interface Parameters : ValueSourceParameters {
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

        /** Optional filter expression. Empty string is treated as no filter. */
        val filter: Property<String>
    }

    private val client: Provider<ArtifactRegistryClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<String>? {
        val parent = RepositoryName.of(
            parameters.projectName.get(),
            parameters.location.get(),
            parameters.repository.get(),
        ).toString()
        val request = ListFilesRequest.newBuilder().apply {
            this.parent = parent
            if (parameters.filter.isPresent) {
                filter = parameters.filter.get()
            }
        }.build()
        return client.get().listFiles(request).iterateAll().map { it.name }
    }
}
