package com.kelvsyc.gradle.google.cloud

import com.kelvsyc.gradle.clients.CredentialReference
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
 *
 * Credential fields ([credentialsJsonRef], [accessTokenRef]) use [CredentialReference] rather than
 * plain strings to ensure that only lookup metadata — not secret values — is serialized to the
 * Gradle configuration cache.
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
     * Reference to where the Google service account JSON key payload can be found. Used when
     * [credentialSource] is [GcpCredentialSource.SERVICE_ACCOUNT_JSON_ENV].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the full service account JSON string. Set via [serviceAccount].
     */
    val credentialsJsonRef: Property<CredentialReference>

    /**
     * Reference to where the static OAuth2 access token can be found. Used when [credentialSource]
     * is [GcpCredentialSource.ACCESS_TOKEN].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the token string. Set via [accessToken].
     */
    val accessTokenRef: Property<CredentialReference>

    /**
     * Path to a credential config JSON file produced by
     * `gcloud iam workload-identity-pools create-cred-config`. Used when [credentialSource] is
     * [GcpCredentialSource.EXTERNAL_ACCOUNT_CONFIG_FILE].
     *
     * Set via [externalAccount].
     */
    val externalAccountConfigFile: RegularFileProperty

    /**
     * Reference to where the credential config JSON payload can be found. Used when [credentialSource]
     * is [GcpCredentialSource.EXTERNAL_ACCOUNT_CONFIG_ENV].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the full credential config JSON string. Set via [externalAccount].
     */
    val externalAccountConfigRef: Property<CredentialReference>

    /**
     * Full workload identity pool provider resource name used as the STS audience. Used when
     * [credentialSource] is [GcpCredentialSource.WORKLOAD_IDENTITY_OIDC].
     *
     * Format: `//iam.googleapis.com/projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/providers/PROVIDER_ID`.
     * Set via [workloadIdentity].
     */
    val workloadIdentityAudience: Property<String>

    /**
     * Reference to where the pre-fetched OIDC token can be found. Used when [credentialSource] is
     * [GcpCredentialSource.WORKLOAD_IDENTITY_OIDC].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the OIDC token string. Resolved lazily at token-refresh time, not at configuration
     * time. Set via [workloadIdentity].
     */
    val workloadIdentityTokenRef: Property<CredentialReference>

    /**
     * Email of the service account to impersonate via
     * [com.google.auth.oauth2.ImpersonatedCredentials]. Used when [credentialSource] is
     * [GcpCredentialSource.WORKLOAD_IDENTITY_OIDC].
     *
     * When set, the STS-exchanged token is further exchanged for a service account access token via
     * the IAM Credentials API. Leave unset to use the STS token directly. Set via [workloadIdentity].
     */
    val workloadIdentityServiceAccountEmail: Property<String>
}
