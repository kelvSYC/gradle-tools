package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.credential.TokenCredential
import com.azure.security.keyvault.secrets.SecretAsyncClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing an asynchronous [SecretAsyncClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.vaultUrl] and [Params.credential] as needed. The same registration can then be shared with
 * value sources and work actions via a `Property<SecretAsyncClientBuildService>` parameter.
 */
abstract class SecretAsyncClientBuildService :
    AbstractClientBuildService<SecretAsyncClient, SecretAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretAsyncClientBuildService].
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

    override fun createClient(): SecretAsyncClient = SecretClientBuilder().apply {
        vaultUrl(parameters.vaultUrl.get())
        if (parameters.credential.isPresent) {
            credential(parameters.credential.get())
        }
    }.buildAsyncClient()
}
