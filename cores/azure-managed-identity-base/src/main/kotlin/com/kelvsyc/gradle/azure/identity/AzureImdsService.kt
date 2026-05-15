package com.kelvsyc.gradle.azure.identity

import com.kelvsyc.gradle.azure.identity.model.AzureAttestedData
import com.kelvsyc.gradle.azure.identity.model.AzureComputeMetadata
import com.kelvsyc.gradle.azure.identity.model.AzureManagedIdentityInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the [Azure Instance Metadata Service](https://learn.microsoft.com/en-us/azure/virtual-machines/instance-metadata-service).
 *
 * All calls require the `Metadata: true` header, which is added globally by
 * [AzureImdsClientBuildService].
 */
interface AzureImdsService {
    /** Retrieves compute metadata for the current VM instance. */
    @GET("instance/compute")
    fun getComputeMetadata(@Query("api-version") apiVersion: String): Call<AzureComputeMetadata>

    /** Retrieves the attested document for the current VM instance. */
    @GET("attested/document")
    fun getAttestedData(
        @Query("api-version") apiVersion: String,
        @Query("nonce") nonce: String? = null,
    ): Call<AzureAttestedData>

    /** Retrieves managed identity information for the current VM instance. */
    @GET("identity/info")
    fun getManagedIdentityInfo(@Query("api-version") apiVersion: String): Call<AzureManagedIdentityInfo>
}
