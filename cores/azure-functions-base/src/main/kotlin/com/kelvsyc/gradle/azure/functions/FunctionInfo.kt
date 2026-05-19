package com.kelvsyc.gradle.azure.functions

/**
 * Non-sensitive metadata describing a single Azure Function, as resolved from the ARM API.
 *
 * This is the client type managed by [FunctionClientBuildService]. It contains only metadata
 * safe to hold in memory — no function keys or access tokens. Keys must be resolved separately
 * at WorkAction execution time via [com.kelvsyc.gradle.clients.CredentialReference].
 *
 * @property name The short function name (final path segment of the ARM resource name).
 * @property invokeUrlTemplate The HTTPS trigger URL template. May contain a `{code}` placeholder
 *   for function-key authentication; this is a structural placeholder, not a resolved secret.
 * @property authLevel The authentication level declared on the function's HTTP trigger.
 */
data class FunctionInfo(
    val name: String,
    val invokeUrlTemplate: String,
    val authLevel: FunctionAuthLevel,
)
