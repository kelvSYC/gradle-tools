package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListVersionsRequest
import com.google.devtools.artifactregistry.v1.PackageName
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of version resource names for a given package.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/repositories/{repository}/packages/{package}/versions/{version}`.
 */
abstract class ListVersionsValueSource : ValueSource<List<String>, ListVersionsValueSource.Parameters> {
    /**
     * Parameters for [ListVersionsValueSource].
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

        /** Package name within the repository. */
        val packageName: Property<String>
    }

    private val client: Provider<ArtifactRegistryClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<String>? {
        val parent = PackageName.of(
            parameters.projectName.get(),
            parameters.location.get(),
            parameters.repository.get(),
            parameters.packageName.get(),
        ).toString()
        val request = ListVersionsRequest.newBuilder().apply {
            this.parent = parent
        }.build()
        return client.get().listVersions(request).iterateAll().map { it.name }
    }
}
