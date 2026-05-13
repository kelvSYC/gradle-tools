package com.kelvsyc.gradle.aws.java.ecr

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
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
        /** The build service managing the ECR client. */
        @get:Internal
        val service: Property<EcrClientBuildService>
    }

    override fun obtain(): Map<String, String>? {
        val request = DescribeRepositoriesRequest.builder().build()

        val response = parameters.service.get().getClient()
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
