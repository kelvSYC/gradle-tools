package com.kelvsyc.gradle.azure.identity

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that queries the Azure IMDS `/attested/document` endpoint and returns the
 * raw `signature` string from the response.
 */
abstract class AzureAttestedDataValueSource :
    ValueSource<String, AzureAttestedDataValueSource.Parameters> {

    /**
     * Parameters for [AzureAttestedDataValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<AzureImdsClientBuildService>

        /** IMDS API version to use. */
        val apiVersion: Property<String>

        /** Optional nonce for replay-attack prevention. */
        val nonce: Property<String>
    }

    override fun obtain(): String? {
        val version = parameters.apiVersion.get()
        val nonce = parameters.nonce.orNull
        val data = parameters.service.get().getClient()
            .getAttestedData(version, nonce)
            .execute()
            .body() ?: return null

        return data.signature
    }
}
