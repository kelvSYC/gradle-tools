package com.kelvsyc.gradle.azure

/**
 * Discriminator for which Azure credential object to construct from [AzureBuildServiceParams].
 *
 * Set via the extension functions on [AzureBuildServiceParams] rather than directly.
 */
enum class AzureCredentialSource {
    /**
     * No credentials.
     *
     * Useful for tests, local emulators (Azurite), and SAS-token-only URLs where authentication is
     * embedded in the endpoint. The build service should skip credential configuration entirely.
     */
    NONE,

    /**
     * Use `DefaultAzureCredentialBuilder().build()`. Resolves credentials from environment
     * variables, managed identities, Azure CLI, IntelliJ, Visual Studio, and other standard
     * sources in order.
     */
    DEFAULT,

    /**
     * Use `ManagedIdentityCredentialBuilder().clientId(clientId).build()`. The
     * [AzureBuildServiceParams.clientId] field is optional — leave it unset to use the
     * system-assigned managed identity, or set it to target a user-assigned managed identity.
     */
    MANAGED_IDENTITY,

    /**
     * Use `ClientSecretCredentialBuilder().tenantId(...).clientId(...).clientSecret(...).build()`,
     * sourced from [AzureBuildServiceParams.tenantId], [AzureBuildServiceParams.clientId], and
     * [AzureBuildServiceParams.clientSecret].
     */
    CLIENT_SECRET,

    /**
     * Use an [com.azure.core.credential.AzureSasCredential] built from
     * [AzureBuildServiceParams.sasToken].
     *
     * Supported by Storage services. Key Vault does not support `AzureSasCredential`.
     */
    SAS_TOKEN,

    /**
     * Use an [com.azure.core.credential.AzureNamedKeyCredential] built from
     * [AzureBuildServiceParams.accountName] and [AzureBuildServiceParams.accountKey].
     *
     * Supported by Storage services. Key Vault does not support `AzureNamedKeyCredential`.
     */
    STORAGE_ACCOUNT_KEY,
}
