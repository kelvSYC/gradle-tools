package com.kelvsyc.gradle.azure

import org.gradle.api.provider.Provider

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
 */
fun AzureBuildServiceParams.clientSecret(
    tenantId: Provider<String>,
    clientId: Provider<String>,
    clientSecret: Provider<String>,
) {
    credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
    this.tenantId.set(tenantId)
    this.clientId.set(clientId)
    this.clientSecret.set(clientSecret)
}

/**
 * Configures these parameters to use a `ClientSecretCredential` for an Azure AD service principal.
 */
fun AzureBuildServiceParams.clientSecret(tenantId: String, clientId: String, clientSecret: String) {
    credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
    this.tenantId.set(tenantId)
    this.clientId.set(clientId)
    this.clientSecret.set(clientSecret)
}

/**
 * Configures these parameters to use an [com.azure.core.credential.AzureSasCredential]. The token
 * must not include the leading `?`. Supported by Storage services; **not** supported by Key Vault.
 */
fun AzureBuildServiceParams.sasToken(token: Provider<String>) {
    credentialSource.set(AzureCredentialSource.SAS_TOKEN)
    sasToken.set(token)
}

/**
 * Configures these parameters to use an [com.azure.core.credential.AzureSasCredential]. The token
 * must not include the leading `?`. Supported by Storage services; **not** supported by Key Vault.
 */
fun AzureBuildServiceParams.sasToken(token: String) {
    credentialSource.set(AzureCredentialSource.SAS_TOKEN)
    sasToken.set(token)
}

/**
 * Configures these parameters to use an [com.azure.core.credential.AzureNamedKeyCredential] sourced
 * from an Azure Storage account name and access key. Supported by Storage services; **not**
 * supported by Key Vault.
 */
fun AzureBuildServiceParams.sharedKey(accountName: Provider<String>, accountKey: Provider<String>) {
    credentialSource.set(AzureCredentialSource.STORAGE_ACCOUNT_KEY)
    this.accountName.set(accountName)
    this.accountKey.set(accountKey)
}

/**
 * Configures these parameters to use an [com.azure.core.credential.AzureNamedKeyCredential] sourced
 * from an Azure Storage account name and access key. Supported by Storage services; **not**
 * supported by Key Vault.
 */
fun AzureBuildServiceParams.sharedKey(accountName: String, accountKey: String) {
    credentialSource.set(AzureCredentialSource.STORAGE_ACCOUNT_KEY)
    this.accountName.set(accountName)
    this.accountKey.set(accountKey)
}
