package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that updates the deployment package (zip file) for a Lambda function.
 *
 * The [Parameters.zipFile] is read from disk and uploaded as the function's new code. When [Parameters.publish]
 * is `true`, AWS publishes a new function version after the update.
 */
abstract class UpdateFunctionCodeAction : WorkAction<UpdateFunctionCodeAction.Parameters> {
    /**
     * Parameters for [UpdateFunctionCodeAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Lambda client. */
        val service: Property<LambdaClientBuildService>

        /** The function name, ARN, or partial ARN to update. */
        val functionName: Property<String>

        /** Path to the deployment package zip file to upload. */
        val zipFile: RegularFileProperty

        /** Whether to publish a new version after the update. Defaults to `false`. */
        val publish: Property<Boolean>
    }

    override fun execute() {
        val bytes = parameters.zipFile.get().asFile.readBytes()
        val request = UpdateFunctionCodeRequest {
            functionName = parameters.functionName.get()
            zipFile = bytes
            publish = parameters.publish.orNull
        }

        runBlocking {
            parameters.service.get().getClient().updateFunctionCode(request)
        }
    }
}
