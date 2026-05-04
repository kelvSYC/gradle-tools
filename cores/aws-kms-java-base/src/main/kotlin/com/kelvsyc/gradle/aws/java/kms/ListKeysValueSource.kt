package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.ListKeysRequest
import kotlin.streams.asSequence

/**
 * [ValueSource] implementation that lists all KMS keys visible to the configured client, returned as a [Map]
 * keyed by key ID with the corresponding key ARN as the value.
 *
 * Pagination is handled internally via the SDK Java paginator.
 */
abstract class ListKeysValueSource : ValueSource<Map<String, String>, ListKeysValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing KMS clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [KmsClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<KmsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = ListKeysRequest.builder().build()

        val response = client.get()
            .listKeysPaginator(request)
            .stream()
            .asSequence()

        return response
            .flatMap { it.keys() }
            .associate {
                it.keyId() to it.keyArn()
            }
    }
}
