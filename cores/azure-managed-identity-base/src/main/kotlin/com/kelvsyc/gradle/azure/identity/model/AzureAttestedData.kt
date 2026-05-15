package com.kelvsyc.gradle.azure.identity.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Attested data returned by the Azure Instance Metadata Service `/attested/document` endpoint.
 */
@JsonClass(generateAdapter = false)
data class AzureAttestedData(
    /** Encoding of the signature (e.g. `pkcs7`). */
    @Json(name = "encoding") val encoding: String,
    /** Base64-encoded signed document. */
    @Json(name = "signature") val signature: String,
)
