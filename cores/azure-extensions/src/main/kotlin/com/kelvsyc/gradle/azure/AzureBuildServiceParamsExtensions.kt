package com.kelvsyc.gradle.azure

import com.kelvsyc.gradle.clients.CredentialReference

/**
 * Configures these parameters to skip credential construction entirely. Suitable for tests, local
 * emulators (Azurite), and SAS-URL access patterns where authentication is embedded in the
 * endpoint.
 */
fun AzureBuildServiceParams.noCredentials() {
    credentialSource.set(AzureCredentialSource.NONE)
}

/**
 * Configures these parameters to use `DefaultAzureCredentialBuilder().build()`, which resolves
 * credentials from environment variables, managed identities, Azure CLI, IntelliJ, Visual Studio,
 * and other standard sources in order.
 */
fun AzureBuildServiceParams.defaultCredential() {
    credentialSource.set(AzureCredentialSource.DEFAULT)
}

/**
 * Configures these parameters to use a `ManagedIdentityCredential`. Pass [clientId] to target a
 * user-assigned managed identity, or omit it to use the system-assigned managed identity.
 */
fun AzureBuildServiceParams.managedIdentity(clientId: String? = null) {
    credentialSource.set(AzureCredentialSource.MANAGED_IDENTITY)
    clientId?.let { this.clientId.set(it) }
}

/**
 * Configures these parameters to use a `ClientSecretCredential` for an Azure AD service principal.
 *
 * [tenantId] and [clientId] are non-sensitive identifiers and are stored as plain strings.
 * [clientSecret] is a [CredentialReference] pointing to an environment variable or system property
 * whose value is the client secret — by default `AZURE_CLIENT_SECRET`. The secret value is
 * resolved at build execution time and never enters the Gradle configuration cache.
 */
fun AzureBuildServiceParams.clientSecret(
    tenantId: String,
    clientId: String,
    clientSecret: CredentialReference = CredentialReference.EnvironmentVariable("AZURE_CLIENT_SECRET"),
) {
    credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
    this.tenantId.set(tenantId)
    this.clientId.set(clientId)
    this.clientSecretRef.set(clientSecret)
}

/**
 * Configures these parameters to use an [com.azure.core.credential.AzureSasCredential]. The token
 * must not include the leading `?`. Supported by Storage services; **not** supported by Key Vault.
 *
 * [token] is a [CredentialReference] pointing to an environment variable or system property whose
 * value is the SAS token — by default `AZURE_STORAGE_SAS_TOKEN`. The token value is resolved at
 * build execution time and never enters the Gradle configuration cache.
 */
fun AzureBuildServiceParams.sasToken(
    token: CredentialReference = CredentialReference.EnvironmentVariable("AZURE_STORAGE_SAS_TOKEN"),
) {
    credentialSource.set(AzureCredentialSource.SAS_TOKEN)
    sasTokenRef.set(token)
}

/**
 * Configures these parameters to use an [com.azure.core.credential.AzureNamedKeyCredential] sourced
 * from an Azure Storage account name and access key. Supported by Storage services; **not**
 * supported by Key Vault.
 *
 * [accountName] is a non-sensitive identifier stored as a plain string. [accountKey] is a
 * [CredentialReference] pointing to an environment variable or system property whose value is the
 * account key — by default `AZURE_STORAGE_ACCOUNT_KEY`. The key value is resolved at build
 * execution time and never enters the Gradle configuration cache.
 */
fun AzureBuildServiceParams.sharedKey(
    accountName: String,
    accountKey: CredentialReference = CredentialReference.EnvironmentVariable("AZURE_STORAGE_ACCOUNT_KEY"),
) {
    credentialSource.set(AzureCredentialSource.STORAGE_ACCOUNT_KEY)
    this.accountName.set(accountName)
    this.accountKeyRef.set(accountKey)
}
