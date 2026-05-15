package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureComputeMetadata
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that queries the Azure IMDS `/instance/compute` endpoint and returns a
 * [Map] of non-null fields from [AzureComputeMetadata].
 *
 * Keys match the JSON field names: `subscriptionId`, `resourceGroupName`, `name`, `location`,
 * `vmId`, `vmSize`, `osType`.
 */
abstract class AzureComputeMetadataValueSource :
    ValueSource<Map<String, String>, AzureComputeMetadataValueSource.Parameters> {

    /**
     * Parameters for [AzureComputeMetadataValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the IMDS client. */
        @get:Internal
        val service: Property<AzureImdsClientBuildService>

        /** IMDS API version to use. Defaults to `2021-02-01`. */
        val apiVersion: Property<String>
    }

    override fun obtain(): Map<String, String>? {
        val version = parameters.apiVersion.getOrElse(DEFAULT_API_VERSION)
        val response = parameters.service.get().getClient()
            .getComputeMetadata(version)
            .execute()
            .body() ?: return null

        return buildMap(response)
    }

    private fun buildMap(metadata: AzureComputeMetadata): Map<String, String> = buildMap {
        metadata.subscriptionId?.let { put("subscriptionId", it) }
        metadata.resourceGroupName?.let { put("resourceGroupName", it) }
        metadata.name?.let { put("name", it) }
        metadata.location?.let { put("location", it) }
        metadata.vmId?.let { put("vmId", it) }
        metadata.vmSize?.let { put("vmSize", it) }
        metadata.osType?.let { put("osType", it) }
    }

    private companion object {
        private const val DEFAULT_API_VERSION = "2021-02-01"
    }
}
