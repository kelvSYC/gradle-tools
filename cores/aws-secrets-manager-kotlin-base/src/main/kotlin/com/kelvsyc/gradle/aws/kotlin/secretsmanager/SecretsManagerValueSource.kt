package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.SecretsManagerException
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation backed by retrieving a secret from Secrets Manager.
 *
 * Only string secrets are supported.
 */
abstract class SecretsManagerValueSource : ValueSource<String, SecretsManagerValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Secrets Manager clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretsManagerClientInfo]. */
        val clientName: Property<String>

        /** The name or ARN of the secret to retrieve. */
        val secretName: Property<String>
    }

    private val client: Provider<SecretsManagerClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = GetSecretValueRequest {
            secretId = parameters.secretName.get()
        }

        return try {
            runBlocking {
                client.get().getSecretValue(request).secretString
            }
        } catch (e: SecretsManagerException) {
            logger.warn("Unable to retrieve secret '${parameters.secretName.get()}' from AWS", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(SecretsManagerValueSource::class.java)
    }
}
