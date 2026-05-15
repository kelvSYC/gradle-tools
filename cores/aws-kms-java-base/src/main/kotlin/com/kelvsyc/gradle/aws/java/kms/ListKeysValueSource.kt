package com.kelvsyc.gradle.aws.java.kms

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.kms.model.ListKeysRequest
import kotlin.streams.asSequence

/**
 * [ValueSource] implementation that lists all KMS keys visible to the configured client, returned as a [Map]
 * keyed by key ID with the corresponding key ARN as the value.
 *
 * Pagination is handled internally via the SDK Java paginator.
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

    override fun obtain(): Map<String, String>? {
        val request = ListKeysRequest.builder().build()

        return parameters.service.get().getClient()
            .listKeysPaginator(request)
            .stream()
            .asSequence()
            .flatMap { it.keys() }
            .associate { it.keyId() to it.keyArn() }
    }
}
