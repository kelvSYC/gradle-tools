package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that invokes a Lambda function.
 *
 * The [Parameters.payload] is sent as the function input (UTF-8 encoded). The [Parameters.invocationType]
 * controls execution semantics (one of `RequestResponse`, `Event`, `DryRun`); when omitted, the default
 * `RequestResponse` is used. This action is fire-and-forget — the response payload is discarded.
 */
abstract class InvokeFunctionAction : WorkAction<InvokeFunctionAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The shared build service managing Lambda clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [LambdaClientInfo]. */
        val clientName: Property<String>

        /** The function name, ARN, or partial ARN to invoke. */
        val functionName: Property<String>

        /** Optional version or alias qualifier. */
        val qualifier: Property<String>

        /** Optional UTF-8 payload sent as the function input. */
        val payload: Property<String>

        /** Optional invocation type (one of `RequestResponse`, `Event`, `DryRun`). */
        val invocationType: Property<String>
    }

    private val client: Provider<LambdaClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = InvokeRequest {
            functionName = parameters.functionName.get()
            qualifier = parameters.qualifier.orNull
            payload = parameters.payload.orNull?.toByteArray()
            invocationType = parameters.invocationType.orNull?.let { InvocationType.fromValue(it) }
        }

        runBlocking {
            client.get().invoke(request)
        }
    }
}
