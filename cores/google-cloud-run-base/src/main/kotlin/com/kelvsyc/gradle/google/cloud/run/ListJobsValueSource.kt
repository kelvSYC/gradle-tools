package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of Cloud Run job short names within a given
 * project and location.
 *
 * Each entry is the short job name (last path segment of the full resource name).
 * Pagination is handled internally via the high-level paged API.
 */
abstract class ListJobsValueSource : ValueSource<List<String>, ListJobsValueSource.Parameters> {

    /**
     * Parameters for [ListJobsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Cloud Run Jobs client.
         */
        @get:Internal
        val service: Property<CloudRunJobsClientBuildService>

        /**
         * GCP project ID.
         */
        val projectId: Property<String>

        /**
         * GCP region, e.g. `"us-central1"`.
         */
        val location: Property<String>
    }

    override fun obtain(): List<String> {
        val parent = "projects/${parameters.projectId.get()}/locations/${parameters.location.get()}"
        return parameters.service.get().getClient()
            .listJobs(parent)
            .iterateAll()
            .map { job -> job.name.substringAfterLast('/') }
    }
}
