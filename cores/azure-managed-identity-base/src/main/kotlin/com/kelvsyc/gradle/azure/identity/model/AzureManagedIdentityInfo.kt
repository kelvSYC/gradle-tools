package com.kelvsyc.gradle.azure.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Managed identity information returned by the Azure Instance Metadata Service `/identity/info` endpoint.
 */
@JsonClass(generateAdapter = false)
data class AzureManagedIdentityInfo(
    /** Client ID of the system-assigned managed identity. */
    @Json(name = "clientId") val clientId: String,
    /** Object ID of the system-assigned managed identity. */
    @Json(name = "objectId") val objectId: String,
)
