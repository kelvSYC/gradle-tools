package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.ListFunctionsRequest
import aws.sdk.kotlin.services.lambda.paginators.listFunctionsPaginated
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
 * [ValueSource] implementation that lists all Lambda functions visible to the configured client, returned as a
 * [Map] keyed by function name with the corresponding function ARN as the value.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow.
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = ListFunctionsRequest {}

        return runBlocking {
            client.get().listFunctionsPaginated(request)
                .flatMapLatest { it.functions?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.functionName!!] = value.functionArn!!
                    }
                }
        }
    }
}
