package com.kelvsyc.gradle.google.cloud

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Configures these parameters to use [com.google.cloud.NoCredentials], i.e. no authentication.
 */
fun GcpBuildServiceParams.noCredentials() {
    credentialSource.set(GcpCredentialSource.NONE)
}

/**
 * Configures these parameters to use
 * [GoogleCredentials.getApplicationDefault][com.google.auth.oauth2.GoogleCredentials.getApplicationDefault],
 * which resolves credentials from `GOOGLE_APPLICATION_CREDENTIALS`, gcloud user credentials,
 * GCE/GKE/Cloud Run metadata, and other standard sources in order.
 */
fun GcpBuildServiceParams.applicationDefault() {
    credentialSource.set(GcpCredentialSource.APPLICATION_DEFAULT)
}

/**
 * Configures these parameters to load
 * [ServiceAccountCredentials][com.google.auth.oauth2.ServiceAccountCredentials] from the supplied
 * JSON key file.
 */
@JvmName("serviceAccountFromFile")
fun GcpBuildServiceParams.serviceAccount(file: Provider<RegularFile>) {
    credentialSource.set(GcpCredentialSource.SERVICE_ACCOUNT_JSON_FILE)
    credentialsFile.set(file)
}

/**
 * Configures these parameters to load
 * [ServiceAccountCredentials][com.google.auth.oauth2.ServiceAccountCredentials] from the supplied
 * inline JSON key payload.
 */
@JvmName("serviceAccountFromJson")
fun GcpBuildServiceParams.serviceAccount(json: Provider<String>) {
    credentialSource.set(GcpCredentialSource.SERVICE_ACCOUNT_JSON_INLINE)
    credentialsJson.set(json)
}

/**
 * Configures these parameters to use a static OAuth2 access token, wrapped in
 * [GoogleCredentials.create][com.google.auth.oauth2.GoogleCredentials.create].
 */
fun GcpBuildServiceParams.accessToken(token: Provider<String>) {
    credentialSource.set(GcpCredentialSource.ACCESS_TOKEN)
    accessToken.set(token)
}

/**
 * Configures these parameters to use a static OAuth2 access token, wrapped in
 * [GoogleCredentials.create][com.google.auth.oauth2.GoogleCredentials.create].
 */
fun GcpBuildServiceParams.accessToken(token: String) {
    credentialSource.set(GcpCredentialSource.ACCESS_TOKEN)
    accessToken.set(token)
}
