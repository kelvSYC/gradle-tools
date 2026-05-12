package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.ListKeysRequest
import aws.sdk.kotlin.services.kms.paginators.listKeysPaginated
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that lists all KMS keys visible to the configured client, returned as a [Map]
 * keyed by key ID with the corresponding key ARN as the value.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow.
 */
abstract class ListKeysValueSource : ValueSource<Map<String, String>, ListKeysValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing KMS clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [KmsClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<KmsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = ListKeysRequest {}

        return runBlocking {
            client.get().listKeysPaginated(request)
                .flatMapLatest { it.keys?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.keyId!!] = value.keyArn!!
                    }
                }
        }
    }
}
