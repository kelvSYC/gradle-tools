package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a map of Cloud Run service names to their HTTPS endpoint
 * URLs within a given project and location.
 *
 * Each entry maps the short service name (last path segment of the full resource name) to the
 * service's HTTPS endpoint URL. Pagination is handled internally via the high-level paged API.
 */
abstract class ListServicesValueSource :
    ValueSource<Map<String, String>, ListServicesValueSource.Parameters> {

    /**
     * Parameters for [ListServicesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Cloud Run Services client.
         */
        @get:Internal
        val service: Property<CloudRunServicesClientBuildService>

        /**
         * GCP project ID.
         */
        val projectId: Property<String>

        /**
         * GCP region, e.g. `"us-central1"`.
         */
        val location: Property<String>
    }

    override fun obtain(): Map<String, String> {
        val parent = "projects/${parameters.projectId.get()}/locations/${parameters.location.get()}"
        return parameters.service.get().getClient()
            .listServices(parent)
            .iterateAll()
            .associate { svc -> svc.name.substringAfterLast('/') to svc.uri }
    }
}
