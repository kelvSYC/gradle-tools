package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.credential.TokenCredential
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a synchronous [SecretClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.vaultUrl] and [Params.credential] as needed. The same registration can then be shared with
 * value sources and work actions via a `Property<SecretClientBuildService>` parameter.
 */
abstract class SecretClientBuildService :
    AbstractClientBuildService<SecretClient, SecretClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The vault URL, e.g. `https://{vaultName}.vault.azure.net`.
         */
        val vaultUrl: Property<String>

        /**
         * The credentials used to authenticate with Azure Key Vault.
         *
         * If unset, the underlying client uses no authentication. Set to
         * `DefaultAzureCredentialBuilder().build()` (from `com.azure:azure-identity`) for Azure's
         * default credential chain.
         */
        val credential: Property<TokenCredential>
    }

    override fun createClient(): SecretClient = SecretClientBuilder().apply {
        vaultUrl(parameters.vaultUrl.get())
        if (parameters.credential.isPresent) {
            credential(parameters.credential.get())
        }
    }.buildClient()
}
