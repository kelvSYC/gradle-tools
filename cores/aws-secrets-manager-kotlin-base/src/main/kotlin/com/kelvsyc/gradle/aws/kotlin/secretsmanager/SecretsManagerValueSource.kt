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
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. A secret value returned
 * here will be stored in `.gradle/configuration-cache/` and is readable by any process with
 * access to the build directory. Retrieve secrets inside a
 * [org.gradle.workers.WorkAction] at task execution time instead, where the value is never
 * written to the cache.
 *
 * [ValueSource] implementation backed by retrieving a secret from Secrets Manager.
 *
 * Only string secrets are supported.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; the secret value " +
        "returned by obtain() is stored in plaintext in .gradle/configuration-cache/. " +
        "Retrieve secrets inside a WorkAction at task execution time instead, " +
        "where the value is never written to the cache.",
    level = DeprecationLevel.WARNING
)
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
