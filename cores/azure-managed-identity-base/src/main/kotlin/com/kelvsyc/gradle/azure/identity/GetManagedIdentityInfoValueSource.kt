package com.kelvsyc.gradle.azure.identity

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that queries the Azure IMDS `/identity/info` endpoint and returns a [Map]
 * with keys `clientId` and `objectId`.
 */
abstract class GetManagedIdentityInfoValueSource :
    ValueSource<Map<String, String>, GetManagedIdentityInfoValueSource.Parameters> {

    /**
     * Parameters for [GetManagedIdentityInfoValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<AzureImdsClientBuildService>

        /** IMDS API version to use. Defaults to `2018-02-01`. */
        val apiVersion: Property<String>
    }

    override fun obtain(): Map<String, String>? {
        val version = parameters.apiVersion.getOrElse(DEFAULT_API_VERSION)
        val info = parameters.service.get().getClient()
            .getManagedIdentityInfo(version)
            .execute()
            .body() ?: return null

        return mapOf("clientId" to info.clientId, "objectId" to info.objectId)
    }

    private companion object {
        private const val DEFAULT_API_VERSION = "2018-02-01"
    }
}
