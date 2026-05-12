package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.paginators.batchGetSecretValuePaginated
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves a set of credentials, by their IDs, from Secrets Manager.
 *
 * Only string secrets are supported.
 */
abstract class SecretBatchValueSource : ValueSource<Map<String, String>, SecretBatchValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Secrets Manager clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretsManagerClientInfo]. */
        val clientName: Property<String>

        /** Set of secret IDs (names or ARNs) to retrieve. */
        val secretIds: SetProperty<String>
    }

    private val client: Provider<SecretsManagerClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = BatchGetSecretValueRequest {
            secretIdList = parameters.secretIds.get().toList()
        }

        return runBlocking {
            client.get().batchGetSecretValuePaginated(request)
                .flatMapLatest { it.secretValues?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.name!!] = value.secretString!!
                    }
                }
        }
    }
}
