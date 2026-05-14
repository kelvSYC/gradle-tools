package com.kelvsyc.gradle.azure

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
     * Azure AD client secret. Used when [credentialSource] is
     * [AzureCredentialSource.CLIENT_SECRET].
     *
     * Set via [clientSecret].
     */
    val clientSecret: Property<String>

    /**
     * Shared Access Signature token, **without** the leading `?`. Used when [credentialSource] is
     * [AzureCredentialSource.SAS_TOKEN].
     *
     * Set via [sasToken].
     */
    val sasToken: Property<String>

    /**
     * Azure Storage account name. Used when [credentialSource] is
     * [AzureCredentialSource.STORAGE_ACCOUNT_KEY].
     *
     * Set via [sharedKey].
     */
    val accountName: Property<String>

    /**
     * Azure Storage account key. Used when [credentialSource] is
     * [AzureCredentialSource.STORAGE_ACCOUNT_KEY].
     *
     * Set via [sharedKey].
     */
    val accountKey: Property<String>
}
