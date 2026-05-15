package com.kelvsyc.gradle.azure

import com.azure.core.credential.AzureNamedKeyCredential
import com.azure.core.credential.AzureSasCredential
import com.azure.core.credential.TokenCredential
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.identity.ManagedIdentityCredentialBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService

/**
 * Abstract base class for Azure SDK client build services with config-cache-safe parameters.
 *
 * Subclass this and implement [createClient], pattern-matching on [resolveCredential] to apply the
 * subset of credential types supported by the underlying Azure client builder:
 *
 * ```kotlin
 * abstract class SecretClientBuildService : AbstractAzureClientBuildService<SecretClient, SecretClientBuildService.Params>() {
 *     interface Params : AzureBuildServiceParams {
 *         val vaultUrl: Property<String>
 *     }
 *
 *     override fun createClient(): SecretClient = SecretClientBuilder().apply {
 *         vaultUrl(parameters.vaultUrl.get())
 *         // Key Vault accepts only TokenCredential; fail loudly on other variants.
 *         when (val c = resolveCredential()) {
 *             null -> {}
 *             is ResolvedAzureCredential.Token -> credential(c.credential)
 *             else -> error("Key Vault supports only TokenCredential credentials; got " + c::class.simpleName)
 *         }
 *     }.buildClient()
 * }
 * ```
 *
 * Configure the service at registration time using the extension functions on
 * [AzureBuildServiceParams]:
 *
 * ```kotlin
 * gradle.sharedServices.registerIfAbsent("kv", SecretClientBuildService::class) {
 *     parameters {
 *         vaultUrl.set("https://example.vault.azure.net")
 *         defaultCredential()
 *     }
 * }
 * ```
 *
 * @param C The Azure client type managed by this build service
 * @param P The [AzureBuildServiceParams] subtype (each service typically subclasses to add an
 *   endpoint property)
 */
abstract class AbstractAzureClientBuildService<C : Any, P : AzureBuildServiceParams>
    : AbstractClientBuildService<C, P>() {

    /**
     * Constructs a [ResolvedAzureCredential] from [AzureBuildServiceParams.credentialSource] and
     * its supporting fields, or returns `null` when [AzureBuildServiceParams.credentialSource] is
     * unset (in which case the calling builder should not have `credential()` called on it, so the
     * Azure SDK applies its own default resolution — typically `DefaultAzureCredential`).
     *
     * | [AzureCredentialSource] | Result |
     * |---|---|
     * | `NONE` | `null` |
     * | `DEFAULT` | [ResolvedAzureCredential.Token] over `DefaultAzureCredentialBuilder().build()` |
     * | `MANAGED_IDENTITY` | [ResolvedAzureCredential.Token] over `ManagedIdentityCredentialBuilder()` (optionally with `clientId`) |
     * | `CLIENT_SECRET` | [ResolvedAzureCredential.Token] over `ClientSecretCredentialBuilder()` using [AzureBuildServiceParams.clientSecretRef] |
     * | `SAS_TOKEN` | [ResolvedAzureCredential.Sas] over [AzureSasCredential] using [AzureBuildServiceParams.sasTokenRef] |
     * | `STORAGE_ACCOUNT_KEY` | [ResolvedAzureCredential.NamedKey] over [AzureNamedKeyCredential] using [AzureBuildServiceParams.accountKeyRef] |
     * | unset | `null` |
     */
    protected fun resolveCredential(): ResolvedAzureCredential? =
        when (parameters.credentialSource.orNull) {
            null, AzureCredentialSource.NONE -> null
            AzureCredentialSource.DEFAULT -> resolveDefault()
            AzureCredentialSource.MANAGED_IDENTITY -> resolveManagedIdentity()
            AzureCredentialSource.CLIENT_SECRET -> resolveClientSecret()
            AzureCredentialSource.SAS_TOKEN -> resolveSasToken()
            AzureCredentialSource.STORAGE_ACCOUNT_KEY -> resolveStorageAccountKey()
        }

    private fun resolveDefault(): ResolvedAzureCredential.Token =
        ResolvedAzureCredential.Token(DefaultAzureCredentialBuilder().build())

    private fun resolveManagedIdentity(): ResolvedAzureCredential.Token {
        val builder = ManagedIdentityCredentialBuilder()
        parameters.clientId.orNull?.let(builder::clientId)
        return ResolvedAzureCredential.Token(builder.build())
    }

    private fun resolveClientSecret(): ResolvedAzureCredential.Token {
        val credential: TokenCredential = ClientSecretCredentialBuilder()
            .tenantId(parameters.tenantId.get())
            .clientId(parameters.clientId.get())
            .clientSecret(parameters.clientSecretRef.get().resolve())
            .build()
        return ResolvedAzureCredential.Token(credential)
    }

    private fun resolveSasToken(): ResolvedAzureCredential.Sas =
        ResolvedAzureCredential.Sas(AzureSasCredential(parameters.sasTokenRef.get().resolve()))

    private fun resolveStorageAccountKey(): ResolvedAzureCredential.NamedKey =
        ResolvedAzureCredential.NamedKey(
            AzureNamedKeyCredential(parameters.accountName.get(), parameters.accountKeyRef.get().resolve())
        )

    /**
     * Convenience wrapper for services that only accept [TokenCredential]-shaped credentials (e.g.
     * Azure Key Vault). Throws [IllegalArgumentException] when [AzureBuildServiceParams] is
     * configured with a Storage-only variant ([AzureCredentialSource.SAS_TOKEN] or
     * [AzureCredentialSource.STORAGE_ACCOUNT_KEY]).
     */
    protected fun resolveTokenCredential(): TokenCredential? = when (val c = resolveCredential()) {
        null -> null
        is ResolvedAzureCredential.Token -> c.credential
        is ResolvedAzureCredential.Sas, is ResolvedAzureCredential.NamedKey ->
            throw IllegalArgumentException(
                "This service supports only TokenCredential-shaped credentials; got " +
                    "${c::class.simpleName}. Use defaultCredential(), managedIdentity(), or " +
                    "clientSecret() instead."
            )
    }
}
