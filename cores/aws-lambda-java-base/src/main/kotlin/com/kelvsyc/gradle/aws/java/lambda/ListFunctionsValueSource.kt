package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest
import kotlin.streams.asSequence
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that lists all Lambda functions visible to the configured client, returned as a
 * [Map] keyed by function name with the corresponding function ARN as the value.
 *
 * Pagination is handled internally via the SDK Java paginator.
 */
abstract class ListFunctionsValueSource : ValueSource<Map<String, String>, ListFunctionsValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Lambda clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [LambdaClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<LambdaClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = ListFunctionsRequest.builder().build()

        val response = client.get()
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
