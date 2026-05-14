package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyRingName
import com.google.cloud.kms.v1.ListCryptoKeysRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of crypto key resource names within a key ring.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
 */
abstract class ListCryptoKeysValueSource : ValueSource<List<String>, ListCryptoKeysValueSource.Parameters> {
    /**
     * Parameters for [ListCryptoKeysValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** GCP project ID. */
        val projectId: Property<String>

        /** GCP location (e.g. `"global"`, `"us-east1"`). */
        val location: Property<String>

        /** Key ring ID. */
        val keyRingId: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = KeyRingName.of(
            parameters.projectId.get(),
            parameters.location.get(),
            parameters.keyRingId.get()
        ).toString()
        val request = ListCryptoKeysRequest.newBuilder().setParent(parent).build()
        return parameters.service.get().getClient().listCryptoKeys(request).iterateAll().map { it.name }
    }
}
