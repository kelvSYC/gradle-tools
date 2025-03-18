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
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

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
