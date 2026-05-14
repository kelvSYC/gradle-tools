package com.kelvsyc.gradle.google.cloud

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Config-cache-safe [BuildServiceParameters] for Google Cloud SDK client build services.
 *
 * All properties are serializable primitives (Strings, files, enums). Do not set them directly;
 * use the extension functions on this interface ([noCredentials], [applicationDefault],
 * [serviceAccount], [accessToken]) which atomically set [credentialSource] and its supporting
 * fields together.
 */
interface GcpBuildServiceParams : BuildServiceParameters {
    /**
     * The Google Cloud project ID, e.g. `"my-project-12345"`.
     *
     * Leave unset to delegate to the SDK's default project resolution (e.g.
     * `GOOGLE_CLOUD_PROJECT` env var, `gcloud config get-value project`).
     */
    val projectId: Property<String>

    /**
     * Which credentials object to construct. Leave unset to delegate to the SDK's default
     * credential resolution (typically [com.google.auth.oauth2.GoogleCredentials.getApplicationDefault]).
     *
     * Use the extension functions rather than setting this directly.
     */
    val credentialSource: Property<GcpCredentialSource>

    /**
     * Path to a Google service account JSON key file. Used when [credentialSource] is
     * [GcpCredentialSource.SERVICE_ACCOUNT_JSON_FILE].
     *
     * Set via [serviceAccount].
     */
    val credentialsFile: RegularFileProperty

    /**
     * Inline Google service account JSON key payload. Used when [credentialSource] is
     * [GcpCredentialSource.SERVICE_ACCOUNT_JSON_INLINE].
     *
     * Set via [serviceAccount].
     */
    val credentialsJson: Property<String>

    /**
     * Static OAuth2 access token string. Used when [credentialSource] is
     * [GcpCredentialSource.ACCESS_TOKEN].
     *
     * Set via [accessToken].
     */
    val accessToken: Property<String>
}
