package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException

/**
 * [ValueSource] implementation backed by retrieving a secret from Secrets Manager.
 *
 * Only string secrets are supported.
 */
abstract class SecretsManagerValueSource : ValueSource<String, SecretsManagerValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    interface Parameters : ValueSourceParameters {
        val client: Property<SecretsManagerClient>

        val secretName: Property<String>
    }

    override fun obtain(): String? {
        val request = GetSecretValueRequest.builder().apply {
            secretId(parameters.secretName.get())
        }.build()

        return try {
            val response = parameters.client.get().getSecretValue(request)
            response.secretString()
        } catch(e: SecretsManagerException) {
            logger.warn(e) { "Unable to retrieve secret '${parameters.secretName.get()}' from AWS" }
            null
        }
    }
}
