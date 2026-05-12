package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.credential.TokenCredential
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property

/**
 * Base configuration interface for Azure Key Vault clients.
 *
 * @param T The Azure Key Vault client type
 */
interface AzureKeyVaultClientInfo<T : Any> : ServiceClientInfo<T> {
    /**
     * The vault URL, e.g. `https://{vaultName}.vault.azure.net`.
     */
    val vaultUrl: Property<String>

    /**
     * The credentials used to authenticate with Azure Key Vault.
     *
     * If absent, the underlying client will use no authentication. Set to
     * `DefaultAzureCredentialBuilder().build()` (from `com.azure:azure-identity`) to use Azure's default credential
     * chain.
     */
    val credential: Property<TokenCredential>
}
