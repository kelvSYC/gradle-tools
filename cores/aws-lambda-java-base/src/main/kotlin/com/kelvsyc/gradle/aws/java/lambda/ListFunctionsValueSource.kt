package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest
import kotlin.streams.asSequence

/**
 * [ValueSource] implementation that lists all Lambda functions visible to the configured client, returned as a
 * [Map] keyed by function name with the corresponding function ARN as the value.
 *
 * Pagination is handled internally via the SDK Java paginator.
 */
abstract class ListFunctionsValueSource : ValueSource<Map<String, String>, ListFunctionsValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Lambda client. */
        @get:Internal
        val service: Property<LambdaClientBuildService>
    }

    override fun obtain(): Map<String, String>? {
        val request = ListFunctionsRequest.builder().build()

        val response = parameters.service.get().getClient()
            .listFunctionsPaginator(request)
            .stream()
            .asSequence()

        return response
            .flatMap { it.functions() }
            .associate {
                it.functionName() to it.functionArn()
            }
    }
}
