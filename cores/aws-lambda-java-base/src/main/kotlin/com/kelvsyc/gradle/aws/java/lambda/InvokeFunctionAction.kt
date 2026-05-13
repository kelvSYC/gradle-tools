package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest

/**
 * [WorkAction] implementation that invokes a Lambda function.
 *
 * The [Parameters.payload] is sent as the function input (UTF-8 encoded). The [Parameters.invocationType]
 * controls execution semantics (one of `RequestResponse`, `Event`, `DryRun`); when omitted, the default
 * `RequestResponse` is used. This action is fire-and-forget — the response payload is discarded.
 */
abstract class InvokeFunctionAction : WorkAction<InvokeFunctionAction.Parameters> {
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
        val request = InvokeRequest.builder().apply {
            functionName(parameters.functionName.get())
            if (parameters.qualifier.isPresent) {
                qualifier(parameters.qualifier.get())
            }
            if (parameters.payload.isPresent) {
                payload(SdkBytes.fromUtf8String(parameters.payload.get()))
            }
            if (parameters.invocationType.isPresent) {
                invocationType(InvocationType.fromValue(parameters.invocationType.get()))
            }
        }.build()

        parameters.service.get().getClient().invoke(request)
    }
}
