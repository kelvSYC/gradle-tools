package com.kelvsyc.gradle.google.cloud

import com.kelvsyc.gradle.clients.CredentialReference
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
 * [ServiceAccountCredentials][com.google.auth.oauth2.ServiceAccountCredentials] from a service
 * account JSON key payload referenced by [json].
 *
 * The [json] reference points to an environment variable or system property whose value is the full
 * service account JSON string. By default, the `GOOGLE_APPLICATION_CREDENTIALS_JSON` environment
 * variable is used — a common convention in CI/CD systems where the key is stored as a secret.
 */
@JvmName("serviceAccountFromJson")
fun GcpBuildServiceParams.serviceAccount(
    json: CredentialReference = CredentialReference.EnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS_JSON"),
) {
    credentialSource.set(GcpCredentialSource.SERVICE_ACCOUNT_JSON_ENV)
    credentialsJsonRef.set(json)
}

/**
 * Configures these parameters to use a static OAuth2 access token referenced by [token], wrapped
 * in [GoogleCredentials.create][com.google.auth.oauth2.GoogleCredentials.create].
 *
 * The [token] reference points to an environment variable or system property whose value is the
 * token string. By default, the `GOOGLE_OAUTH2_TOKEN` environment variable is used.
 */
fun GcpBuildServiceParams.accessToken(
    token: CredentialReference = CredentialReference.EnvironmentVariable("GOOGLE_OAUTH2_TOKEN"),
) {
    credentialSource.set(GcpCredentialSource.ACCESS_TOKEN)
    accessTokenRef.set(token)
}
