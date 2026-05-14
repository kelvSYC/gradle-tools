package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing a synchronous [SecretClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [Params.vaultUrl] and the credential source via the
 * [AzureBuildServiceParams] extension functions
 * ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret]).
 *
 * Azure Key Vault only accepts `TokenCredential`-shaped credentials; configuring this service
 * with [sasToken][com.kelvsyc.gradle.azure.sasToken] or
 * [sharedKey][com.kelvsyc.gradle.azure.sharedKey] will fail at execution time with an
 * [IllegalArgumentException] from [resolveTokenCredential].
 */
abstract class SecretClientBuildService :
    AbstractAzureClientBuildService<SecretClient, SecretClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /**
         * The vault URL, e.g. `https://{vaultName}.vault.azure.net`.
         */
        val vaultUrl: Property<String>
    }

    override fun createClient(): SecretClient = SecretClientBuilder().apply {
        vaultUrl(parameters.vaultUrl.get())
        resolveTokenCredential()?.let(::credential)
    }.buildClient()
}
