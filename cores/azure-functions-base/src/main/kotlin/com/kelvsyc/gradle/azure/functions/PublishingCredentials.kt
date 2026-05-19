package com.kelvsyc.gradle.azure.functions

/**
 * Data class representing publishing credentials for a function app.
 *
 * Publishing credentials are used for deployment operations via the Kudu SCM endpoint and
 * authenticate with HTTP Basic auth (username + password).
 */
data class PublishingCredentials(
    val publishingUserName: String,
    val publishingPassword: String,
)

