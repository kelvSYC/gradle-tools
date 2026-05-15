package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
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
        val request = GetSecretValueRequest.builder()
            .secretId(parameters.secretName.get())
            .build()

        return try {
            parameters.service.get().getClient().getSecretValue(request).secretString()
        } catch (e: SecretsManagerException) {
            logger.warn(e) { "Unable to retrieve secret '${parameters.secretName.get()}' from AWS" }
            null
        }
    }
}
