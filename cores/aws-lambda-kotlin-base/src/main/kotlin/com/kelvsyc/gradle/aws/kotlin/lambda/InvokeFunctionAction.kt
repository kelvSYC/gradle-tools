package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
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
    /**
     * Parameters for [InvokeFunctionAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Lambda client. */
        @get:Internal
        val service: Property<LambdaClientBuildService>

        /** The function name, ARN, or partial ARN to invoke. */
        val functionName: Property<String>

        /** Optional version or alias qualifier. */
        val qualifier: Property<String>

        /** Optional UTF-8 payload sent as the function input. */
        val payload: Property<String>

        /** Optional invocation type (one of `RequestResponse`, `Event`, `DryRun`). */
        val invocationType: Property<String>
    }

    override fun execute() {
        val request = InvokeRequest {
            functionName = parameters.functionName.get()
            qualifier = parameters.qualifier.orNull
            payload = parameters.payload.orNull?.toByteArray()
            invocationType = parameters.invocationType.orNull?.let { InvocationType.fromValue(it) }
        }

        runBlocking {
            parameters.service.get().getClient().invoke(request)
        }
    }
}
