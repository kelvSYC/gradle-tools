package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.model.ListKeysRequest
import aws.sdk.kotlin.services.kms.paginators.listKeysPaginated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
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
    /**
     * Parameters for [ListKeysValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = ListKeysRequest {}
        val client = parameters.service.get().getClient()

        return runBlocking {
            client.listKeysPaginated(request)
                .flatMapLatest { it.keys?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.keyId!!] = value.keyArn!!
                    }
                }
        }
    }
}
