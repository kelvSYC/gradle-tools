package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.ListKeyRingsRequest
import com.google.cloud.kms.v1.LocationName
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of key ring resource names within a GCP project
 * location.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/keyRings/{keyRing}`.
 */
abstract class ListKeyRingsValueSource : ValueSource<List<String>, ListKeyRingsValueSource.Parameters> {
    /**
     * Parameters for [ListKeyRingsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** GCP location (e.g. `"global"`, `"us-east1"`). */
        val location: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = LocationName.of(parameters.projectId.get(), parameters.location.get()).toString()
        val request = ListKeyRingsRequest.newBuilder().setParent(parent).build()
        return parameters.service.get().getClient().listKeyRings(request).iterateAll().map { it.name }
    }
}
