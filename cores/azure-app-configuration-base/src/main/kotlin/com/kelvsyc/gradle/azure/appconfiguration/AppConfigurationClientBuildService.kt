package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.ConfigurationClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property

/**
 * Build service managing a synchronous [ConfigurationClient] instance for Azure App Configuration.
 *
 * Supports two authentication modes:
 * - **Connection string** (development): set [Params.connectionStringRef] to a [CredentialReference]
 *   resolving the connection string. Endpoint is embedded in the string.
 * - **Token credential** (production): set [Params.endpoint] and configure the credential source
 *   via [AzureBuildServiceParams] extension functions
 *   ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 *   [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 *   [clientSecret][com.kelvsyc.gradle.azure.clientSecret]).
 *
 * SAS and named-key credential variants are rejected with [IllegalArgumentException] at
 * initialization — Azure App Configuration accepts only token-shaped credentials.
 *
 * Register via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent].
 */
abstract class AppConfigurationClientBuildService :
    AbstractAzureClientBuildService<ConfigurationClient, AppConfigurationClientBuildService.Params>() {

    /**
     * Configuration parameters for [AppConfigurationClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /**
         * The App Configuration store endpoint URL, e.g. `https://{store}.azconfig.io`.
         * Optional when [connectionStringRef] is set (the endpoint is embedded in the connection string).
         */
        val endpoint: Property<String>

        /**
         * Reference to the App Configuration connection string (stored in an environment variable
         * or system property). When set, takes priority over [endpoint] and the credential source.
         *
         * Prefer token credentials in production; connection strings are most useful in local
         * development or CI where managed identity is unavailable.
         */
        val connectionStringRef: Property<CredentialReference>
    }

    override fun createClient(): ConfigurationClient = ConfigurationClientBuilder().apply {
        if (parameters.connectionStringRef.isPresent) {
            connectionString(parameters.connectionStringRef.get().resolve())
        } else {
            endpoint(parameters.endpoint.get())
            resolveTokenCredential()?.let(::credential)
        }
    }.buildClient()
}
