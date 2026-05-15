package com.kelvsyc.gradle.aws.java.secretsmanager

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.secretsmanager.model.BatchGetSecretValueRequest
import kotlin.streams.asSequence

/**
 * [ValueSource] implementation that retrieves a set of secrets, by their IDs, from Secrets Manager.
 *
 * Only string secrets are supported.
 */
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
