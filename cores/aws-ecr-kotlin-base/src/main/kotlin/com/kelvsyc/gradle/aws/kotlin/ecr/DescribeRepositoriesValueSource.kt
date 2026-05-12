package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.DescribeRepositoriesRequest
import aws.sdk.kotlin.services.ecr.paginators.describeRepositoriesPaginated
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
 * [ValueSource] implementation that lists ECR repositories visible to the configured client, returned as a
 * [Map] keyed by repository name with the corresponding repository URI as the value.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow.
 */
abstract class DescribeRepositoriesValueSource : ValueSource<Map<String, String>, DescribeRepositoriesValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing ECR clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [EcrClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<EcrClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = DescribeRepositoriesRequest {}

        return runBlocking {
            client.get().describeRepositoriesPaginated(request)
                .flatMapLatest { it.repositories?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.repositoryName!!] = value.repositoryUri!!
                    }
                }
        }
    }
}
