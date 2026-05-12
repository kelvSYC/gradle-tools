package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.exception.HttpResponseException
import com.azure.security.keyvault.secrets.SecretClient
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation backed by retrieving a secret from Azure Key Vault.
 *
 * The secret value is returned as a string.
 */
abstract class KeyVaultSecretValueSource : ValueSource<String, KeyVaultSecretValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Key Vault clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretClientInfo]. */
        val clientName: Property<String>

        /** The name of the secret to retrieve. */
        val secretName: Property<String>

        /** The version of the secret to retrieve. If absent, the latest version is used. */
        val version: Property<String>
    }

    private val client: Provider<SecretClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        return try {
            val secret = if (parameters.version.isPresent) {
                client.get().getSecret(parameters.secretName.get(), parameters.version.get())
            } else {
                client.get().getSecret(parameters.secretName.get())
            }
            secret.value
        } catch (e: HttpResponseException) {
            logger.warn("Unable to retrieve secret '${parameters.secretName.get()}' from Azure Key Vault", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(KeyVaultSecretValueSource::class.java)
    }
}
