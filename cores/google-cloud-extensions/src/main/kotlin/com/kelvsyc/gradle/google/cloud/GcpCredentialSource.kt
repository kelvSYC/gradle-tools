package com.kelvsyc.gradle.google.cloud

/**
 * Discriminator for which Google Cloud credentials object to construct from [GcpBuildServiceParams].
 *
 * Set via the extension functions on [GcpBuildServiceParams] rather than directly.
 */
enum class GcpCredentialSource {
    /**
     * Use [com.google.cloud.NoCredentials], i.e. no authentication.
     *
     * Useful for tests, local emulators (e.g. Pub/Sub emulator), and public-bucket access. Maps to
     * [com.google.api.gax.core.NoCredentialsProvider] when a `CredentialsProvider` is required.
     */
    NONE,

    /**
     * Use [com.google.auth.oauth2.GoogleCredentials.getApplicationDefault], which resolves
     * credentials from `GOOGLE_APPLICATION_CREDENTIALS`, gcloud user credentials, GCE/GKE/Cloud Run
     * metadata, and other standard sources in order.
     */
    APPLICATION_DEFAULT,

    /**
     * Load a [com.google.auth.oauth2.ServiceAccountCredentials] from the JSON key file at
     * [GcpBuildServiceParams.credentialsFile].
     */
    SERVICE_ACCOUNT_JSON_FILE,

    /**
     * Load a [com.google.auth.oauth2.ServiceAccountCredentials] from the JSON key payload in
     * [GcpBuildServiceParams.credentialsJson].
     */
    SERVICE_ACCOUNT_JSON_INLINE,

    /**
     * Build a [com.google.auth.oauth2.GoogleCredentials] from a static OAuth2 access token at
     * [GcpBuildServiceParams.accessToken].
     */
    ACCESS_TOKEN,
}
