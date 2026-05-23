package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that invokes a Lambda function.
 *
 * The [payload] is sent as the function input (UTF-8 encoded). The [invocationType]
 * controls execution semantics (one of `RequestResponse`, `Event`, `DryRun`); when omitted, the default
 * `RequestResponse` is used. This task is fire-and-forget — the response payload is discarded.
 */
@UntrackedTask(because = "Communicates with AWS Lambda; no local output")
abstract class InvokeFunction : DefaultTask() {

    /** The build service managing the Lambda client. */
    @get:Internal
    abstract val service: Property<LambdaClientBuildService>

    /** The function name, ARN, or partial ARN to invoke. */
    @get:Input
    abstract val functionName: Property<String>

    /** Optional version or alias qualifier. */
    @get:Input
    @get:Optional
    abstract val qualifier: Property<String>

    /** Optional UTF-8 payload sent as the function input. */
    @get:Input
    @get:Optional
    abstract val payload: Property<String>

    /** Optional invocation type (one of `RequestResponse`, `Event`, `DryRun`). */
    @get:Input
    @get:Optional
    abstract val invocationType: Property<String>

    @TaskAction
    fun execute() {
        val request = InvokeRequest {
            functionName = this@InvokeFunction.functionName.get()
            qualifier = this@InvokeFunction.qualifier.orNull
            payload = this@InvokeFunction.payload.orNull?.toByteArray()
            invocationType = this@InvokeFunction.invocationType.orNull?.let { InvocationType.fromValue(it) }
        }

        runBlocking {
            service.get().getClient().invoke(request)
        }
    }
}
