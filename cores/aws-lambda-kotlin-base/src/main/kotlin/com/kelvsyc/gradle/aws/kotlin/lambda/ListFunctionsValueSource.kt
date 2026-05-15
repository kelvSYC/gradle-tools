package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.model.ListFunctionsRequest
import aws.sdk.kotlin.services.lambda.paginators.listFunctionsPaginated
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
 * [ValueSource] implementation that lists all Lambda functions visible to the configured client, returned as a
 * [Map] keyed by function name with the corresponding function ARN as the value.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow.
 */
abstract class ListFunctionsValueSource : ValueSource<Map<String, String>, ListFunctionsValueSource.Parameters> {
    /**
     * Parameters for [ListFunctionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Lambda client. */
        @get:Internal
        val service: Property<LambdaClientBuildService>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = ListFunctionsRequest {}
        val client = parameters.service.get().getClient()

        return runBlocking {
            client.listFunctionsPaginated(request)
                .flatMapLatest { it.functions?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.functionName!!] = value.functionArn!!
                    }
                }
        }
    }
}
