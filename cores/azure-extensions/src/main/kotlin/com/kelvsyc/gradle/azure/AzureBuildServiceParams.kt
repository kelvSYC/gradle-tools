package com.kelvsyc.gradle.azure

import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Config-cache-safe [BuildServiceParameters] for Azure SDK client build services.
 *
 * All properties are serializable primitives (Strings, enums). Do not set them directly; use the
 * extension functions on this interface ([noCredentials], [defaultCredential], [managedIdentity],
 * [clientSecret], [sasToken], [sharedKey]) which atomically set [credentialSource] and its
 * supporting fields together.
 *
 * Credential fields ([clientSecretRef], [sasTokenRef], [accountKeyRef]) use [CredentialReference]
 * rather than plain strings to ensure that only lookup metadata — not secret values — is serialized
 * to the Gradle configuration cache. Non-secret identity fields ([tenantId], [clientId],
 * [accountName]) remain plain strings.
 *
 * Note that the service-specific endpoint (Storage account URL, Key Vault URL, etc.) is **not**
 * part of this interface — each service builds upon this interface with its own endpoint property
 * because the endpoint shape differs per service.
 */
interface AzureBuildServiceParams : BuildServiceParameters {
    /**
     * Which credential object to construct. Leave unset to delegate to the Azure SDK's default
     * resolution (typically `DefaultAzureCredential`).
     *
     * Use the extension functions rather than setting this directly.
     */
    val credentialSource: Property<AzureCredentialSource>

    /**
     * Azure AD tenant ID. Used when [credentialSource] is [AzureCredentialSource.CLIENT_SECRET].
     *
     * Set via [clientSecret].
     */
    val tenantId: Property<String>

    /**
     * Azure AD application (client) ID. Used when [credentialSource] is
     * [AzureCredentialSource.CLIENT_SECRET] or [AzureCredentialSource.MANAGED_IDENTITY] (optional
     * for the latter).
     *
     * Set via [clientSecret] or [managedIdentity].
     */
    val clientId: Property<String>

    /**
     * Reference to where the Azure AD client secret can be found. Used when [credentialSource] is
     * [AzureCredentialSource.CLIENT_SECRET].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the client secret. Set via [clientSecret].
     */
    val clientSecretRef: Property<CredentialReference>

    /**
     * Reference to where the Shared Access Signature token can be found, **without** the leading
     * `?`. Used when [credentialSource] is [AzureCredentialSource.SAS_TOKEN].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the SAS token. Set via [sasToken].
     */
    val sasTokenRef: Property<CredentialReference>

    /**
     * Azure Storage account name. Used when [credentialSource] is
     * [AzureCredentialSource.STORAGE_ACCOUNT_KEY].
     *
     * Set via [sharedKey].
     */
    val accountName: Property<String>

    /**
     * Reference to where the Azure Storage account key can be found. Used when [credentialSource]
     * is [AzureCredentialSource.STORAGE_ACCOUNT_KEY].
     *
     * Stores a [CredentialReference] pointing to an environment variable or system property whose
     * value is the account key. Set via [sharedKey].
     */
    val accountKeyRef: Property<CredentialReference>
}
