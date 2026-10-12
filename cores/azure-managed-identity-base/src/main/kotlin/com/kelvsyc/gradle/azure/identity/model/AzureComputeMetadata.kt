package com.kelvsyc.gradle.azure.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Compute metadata returned by the Azure Instance Metadata Service `/instance/compute` endpoint.
 */
@JsonClass(generateAdapter = false)
data class AzureComputeMetadata(
    /** Azure subscription ID in which the VM is deployed. */
    @param:Json(name = "subscriptionId") val subscriptionId: String? = null,
    /** Resource group containing the VM. */
    @param:Json(name = "resourceGroupName") val resourceGroupName: String? = null,
    /** Name of the VM. */
    @param:Json(name = "name") val name: String? = null,
    /** Azure region where the VM is located. */
    @param:Json(name = "location") val location: String? = null,
    /** Unique identifier for the VM. */
    @param:Json(name = "vmId") val vmId: String? = null,
    /** VM size (SKU). */
    @param:Json(name = "vmSize") val vmSize: String? = null,
    /** OS type (`Linux` or `Windows`). */
    @param:Json(name = "osType") val osType: String? = null,
)
