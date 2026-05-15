package com.kelvsyc.gradle.aws.java.secretsmanager

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.secretsmanager.model.BatchGetSecretValueRequest
import kotlin.streams.asSequence

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. Secret values returned
 * here will be stored in `.gradle/configuration-cache/` and are readable by any process with
 * access to the build directory. Retrieve secrets inside a
 * [org.gradle.workers.WorkAction] at task execution time instead, where the value is never
 * written to the cache.
 *
 * [ValueSource] implementation that retrieves a set of secrets, by their IDs, from Secrets Manager.
 *
 * Only string secrets are supported.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; secret values " +
        "returned by obtain() are stored in plaintext in .gradle/configuration-cache/. " +
        "Retrieve secrets inside a WorkAction at task execution time instead, " +
        "where the value is never written to the cache.",
    level = DeprecationLevel.WARNING
)
abstract class SecretBatchValueSource : ValueSource<Map<String, String>, SecretBatchValueSource.Parameters> {
    /**
     * Parameters for [SecretBatchValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Secrets Manager client. */
        @get:Internal
        val service: Property<SecretsManagerClientBuildService>

        /** Set of secret IDs (names or ARNs) to retrieve. */
        val secretIds: SetProperty<String>
    }

    override fun obtain(): Map<String, String>? {
        val request = BatchGetSecretValueRequest.builder()
            .secretIdList(parameters.secretIds.get())
            .build()

        val response = parameters.service.get().getClient()
            .batchGetSecretValuePaginator(request)
            .stream()
            .asSequence()

        return response
            .flatMap { it.secretValues() }
            .associate { it.name() to it.secretString() }
    }
}
