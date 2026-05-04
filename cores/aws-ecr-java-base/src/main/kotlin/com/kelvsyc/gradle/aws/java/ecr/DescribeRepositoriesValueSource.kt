package com.kelvsyc.gradle.aws.java.ecr

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest
import kotlin.streams.asSequence

/**
 * [ValueSource] implementation that lists ECR repositories visible to the configured client, returned as a
 * [Map] keyed by repository name with the corresponding repository URI as the value.
 *
 * Pagination is handled internally via the SDK Java paginator.
 */
abstract class DescribeRepositoriesValueSource : ValueSource<Map<String, String>, DescribeRepositoriesValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing ECR clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of an [EcrClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<EcrClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = DescribeRepositoriesRequest.builder().build()

        val response = client.get()
            .describeRepositoriesPaginator(request)
            .stream()
            .asSequence()

        return response
            .flatMap { it.repositories() }
            .associate {
                it.repositoryName() to it.repositoryUri()
            }
    }
}
