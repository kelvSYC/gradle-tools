package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.SecretsManagerException
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation backed by retrieving a secret from Secrets Manager.
 *
 * Only string secrets are supported.
 */
abstract class SecretsManagerValueSource : ValueSource<String, SecretsManagerValueSource.Parameters> {
    /**
     * Parameters for [SecretsManagerValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Secrets Manager client. */
        @get:Internal
        val service: Property<SecretsManagerClientBuildService>

        /** The name or ARN of the secret to retrieve. */
        val secretName: Property<String>
    }

    override fun obtain(): String? {
        val request = GetSecretValueRequest {
            secretId = parameters.secretName.get()
        }

        return try {
            runBlocking {
                parameters.service.get().getClient().getSecretValue(request).secretString
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
