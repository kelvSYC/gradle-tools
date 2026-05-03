package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.BatchGetSecretValueRequest
import kotlin.streams.asSequence

/**
 * [ValueSource] implementation that retrieves a set of credentials, by their IDs, from Secrets Manager.
 *
 * Only string secrets are supported.
 */
abstract class SecretBatchValueSource : ValueSource<Map<String, String>, SecretBatchValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Secrets Manager clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretsManagerClientInfo]. */
        val clientName: Property<String>

        /** Set of secret IDs (names or ARNs) to retrieve. */
        val secretIds: SetProperty<String>
    }

    private val client: Provider<SecretsManagerClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = BatchGetSecretValueRequest.builder().apply {
            secretIdList(parameters.secretIds.get())
        }.build()

        val response = client.get()
            .batchGetSecretValuePaginator(request)
            .stream()
            .asSequence()

        return response
            .flatMap { it.secretValues() }
            .associate {
                it.name() to it.secretString()
            }
    }
}
