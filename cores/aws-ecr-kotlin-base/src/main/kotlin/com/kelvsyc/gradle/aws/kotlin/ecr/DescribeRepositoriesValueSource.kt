package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.model.DescribeRepositoriesRequest
import aws.sdk.kotlin.services.ecr.paginators.describeRepositoriesPaginated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation that lists ECR repositories visible to the configured client, returned as a
 * [Map] keyed by repository name with the corresponding repository URI as the value.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow.
 */
abstract class DescribeRepositoriesValueSource :
    ValueSource<Map<String, String>, DescribeRepositoriesValueSource.Parameters> {
    /**
     * Parameters for [DescribeRepositoriesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the ECR client. */
        val service: Property<EcrClientBuildService>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = DescribeRepositoriesRequest {}
        val client = parameters.service.get().getClient()

        return runBlocking {
            client.describeRepositoriesPaginated(request)
                .flatMapLatest { it.repositories?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.repositoryName!!] = value.repositoryUri!!
                    }
                }
        }
    }
}
