package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that updates the deployment package (zip file) for a Lambda function.
 *
 * The [zipFile] is read from disk and uploaded as the function's new code. When [publish]
 * is `true`, AWS publishes a new function version after the update.
 */
@UntrackedTask(because = "Communicates with AWS Lambda; no local output")
abstract class UpdateFunctionCode : DefaultTask() {

    /** The build service managing the Lambda client. */
    @get:Internal
    abstract val service: Property<LambdaClientBuildService>

    /** The function name, ARN, or partial ARN to update. */
    @get:Input
    abstract val functionName: Property<String>

    /** Path to the deployment package zip file to upload. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val zipFile: RegularFileProperty

    /** Whether to publish a new version after the update. Defaults to `false`. */
    @get:Input
    @get:Optional
    abstract val publish: Property<Boolean>

    @TaskAction
    fun execute() {
        val bytes = zipFile.get().asFile.readBytes()
        val request = UpdateFunctionCodeRequest {
            functionName = this@UpdateFunctionCode.functionName.get()
            zipFile = bytes
            publish = this@UpdateFunctionCode.publish.orNull
        }

        runBlocking {
            service.get().getClient().updateFunctionCode(request)
        }
    }
}
