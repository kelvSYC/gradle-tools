package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.ListFunctionsRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a map of Cloud Functions Gen 2 function names to their
 * HTTPS trigger URIs within a given project and location.
 *
 * Each entry maps the short function name (last path segment of the full resource name) to the
 * function's HTTPS trigger URI. Pagination is handled internally via the high-level paged API.
 */
abstract class ListFunctionsValueSource :
    ValueSource<Map<String, String>, ListFunctionsValueSource.Parameters> {

    /**
     * Parameters for [ListFunctionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Cloud Functions client. */
        @get:Internal
        val service: Property<FunctionServiceClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** GCP region, e.g. `"us-central1"`. */
        val location: Property<String>
    }

    override fun obtain(): Map<String, String>? {
        val parent = "projects/${parameters.projectId.get()}/locations/${parameters.location.get()}"
        val request = ListFunctionsRequest.newBuilder()
            .setParent(parent)
            .build()
        return parameters.service.get().getClient()
            .listFunctions(request)
            .iterateAll()
            .associate { fn -> fn.name.substringAfterLast('/') to fn.serviceConfig.uri }
    }
}
