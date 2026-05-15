package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.exception.HttpResponseException
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. A secret value returned
 * here will be stored in `.gradle/configuration-cache/` and is readable by any process with
 * access to the build directory. Retrieve secrets inside a
 * [org.gradle.workers.WorkAction] at task execution time instead, where the value is never
 * written to the cache.
 *
 * [ValueSource] implementation backed by retrieving a secret from Azure Key Vault.
 *
 * The secret value is returned as a string.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; the secret value " +
        "returned by obtain() is stored in plaintext in .gradle/configuration-cache/. " +
        "Retrieve secrets inside a WorkAction at task execution time instead, " +
        "where the value is never written to the cache.",
    level = DeprecationLevel.WARNING
)
abstract class KeyVaultSecretValueSource : ValueSource<String, KeyVaultSecretValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Key Vault secret client. */
        @get:Internal
        val service: Property<SecretClientBuildService>

        /** The name of the secret to retrieve. */
        val secretName: Property<String>

        /** The version of the secret to retrieve. If absent, the latest version is used. */
        val version: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val client = parameters.service.get().getClient()
            val secret = if (parameters.version.isPresent) {
                client.getSecret(parameters.secretName.get(), parameters.version.get())
            } else {
                client.getSecret(parameters.secretName.get())
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
