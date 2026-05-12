package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.paginators.batchGetSecretValuePaginated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

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
        val service: Property<SecretsManagerClientBuildService>

        /** Set of secret IDs (names or ARNs) to retrieve. */
        val secretIds: SetProperty<String>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = BatchGetSecretValueRequest {
            secretIdList = parameters.secretIds.get().toList()
        }
        val client = parameters.service.get().getClient()

        return runBlocking {
            client.batchGetSecretValuePaginated(request)
                .flatMapLatest { it.secretValues?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.name!!] = value.secretString!!
                    }
                }
        }
    }
}
