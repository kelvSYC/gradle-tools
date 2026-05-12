package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] implementation that updates the deployment package (zip file) for a Lambda function.
 *
 * The [Parameters.zipFile] is read from disk and uploaded as the function's new code. When [Parameters.publish]
 * is `true`, AWS publishes a new function version after the update.
 */
abstract class UpdateFunctionCodeAction : WorkAction<UpdateFunctionCodeAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The shared build service managing Lambda clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [LambdaClientInfo]. */
        val clientName: Property<String>

        /** The function name, ARN, or partial ARN to update. */
        val functionName: Property<String>

        /** Path to the deployment package zip file to upload. */
        val zipFile: RegularFileProperty

        /** Whether to publish a new version after the update. Defaults to `false`. */
        val publish: Property<Boolean>
    }

    private val client: Provider<LambdaClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val bytes = parameters.zipFile.get().asFile.readBytes()
        val request = UpdateFunctionCodeRequest {
            functionName = parameters.functionName.get()
            zipFile = bytes
            publish = parameters.publish.orNull
        }

        runBlocking {
            client.get().updateFunctionCode(request)
        }
    }
}
