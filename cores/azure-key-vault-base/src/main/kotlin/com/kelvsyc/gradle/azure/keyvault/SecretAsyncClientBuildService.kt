package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretAsyncClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing an asynchronous [SecretAsyncClient] instance.
 *
 * See [SecretClientBuildService] for the synchronous equivalent and credential configuration. Like
 * the synchronous service, this build service only supports `TokenCredential`-shaped credentials.
 */
abstract class SecretAsyncClientBuildService :
    AbstractAzureClientBuildService<SecretAsyncClient, SecretAsyncClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretAsyncClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /**
         * The vault URL, e.g. `https://{vaultName}.vault.azure.net`.
         */
        val vaultUrl: Property<String>
    }

    override fun createClient(): SecretAsyncClient = SecretClientBuilder().apply {
        vaultUrl(parameters.vaultUrl.get())
        resolveTokenCredential()?.let(::credential)
    }.buildAsyncClient()
}
