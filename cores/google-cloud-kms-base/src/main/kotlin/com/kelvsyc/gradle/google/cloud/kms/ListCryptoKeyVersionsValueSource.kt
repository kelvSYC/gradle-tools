package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.ListCryptoKeyVersionsRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of crypto key version resource names within a
 * crypto key.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
 */
abstract class ListCryptoKeyVersionsValueSource :
    ValueSource<List<String>, ListCryptoKeyVersionsValueSource.Parameters> {
    /**
     * Parameters for [ListCryptoKeyVersionsValueSource].
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

        /** Crypto key ID. */
        val cryptoKeyId: Property<String>
    }

    override fun obtain(): List<String>? {
        val parent = CryptoKeyName.of(
            parameters.projectId.get(),
            parameters.location.get(),
            parameters.keyRingId.get(),
            parameters.cryptoKeyId.get()
        ).toString()
        val request = ListCryptoKeyVersionsRequest.newBuilder().setParent(parent).build()
        return parameters.service.get().getClient().listCryptoKeyVersions(request).iterateAll().map { it.name }
    }
}
