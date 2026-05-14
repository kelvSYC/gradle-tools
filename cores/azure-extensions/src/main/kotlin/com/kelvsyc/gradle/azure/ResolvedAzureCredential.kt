package com.kelvsyc.gradle.azure

import com.azure.core.credential.AzureNamedKeyCredential
import com.azure.core.credential.AzureSasCredential
import com.azure.core.credential.TokenCredential

/**
 * The reified credential produced by [AbstractAzureClientBuildService.resolveCredential] from a
 * [AzureBuildServiceParams] configuration.
 *
 * Azure client builders accept different credential types depending on the service: Key Vault
 * accepts only [TokenCredential], whereas Blob Storage accepts [TokenCredential],
 * [AzureSasCredential], or [AzureNamedKeyCredential]. Subclasses pattern-match on this sealed
 * hierarchy and apply only the variants supported by their underlying builder.
 */
sealed interface ResolvedAzureCredential {
    /**
     * An OAuth2 [TokenCredential], such as one produced by `DefaultAzureCredential`,
     * `ManagedIdentityCredential`, or `ClientSecretCredential`.
     */
    @JvmInline
    value class Token(val credential: TokenCredential) : ResolvedAzureCredential

    /** A Shared Access Signature credential. Supported by Storage services. */
    @JvmInline
    value class Sas(val credential: AzureSasCredential) : ResolvedAzureCredential

    /** A named-key credential (Storage account name + key). Supported by Storage services. */
    @JvmInline
    value class NamedKey(val credential: AzureNamedKeyCredential) : ResolvedAzureCredential
}
