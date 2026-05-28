package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest

/**
 * Task that updates the deployment package (zip file) for a single Lambda function.
 *
 * The [zipFile] is read from disk and uploaded as the function's new code. When [publish]
 * is `true`, AWS publishes a new function version after the update.
 *
 * For updating multiple functions in a single task, use [BatchUpdateFunctionCode] instead.
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class UpdateFunctionCodeTask : DefaultTask() {

    /** The build service managing the Lambda client. */
    @get:ServiceReference
    abstract val service: Property<LambdaClientBuildService>

    /** The function name, ARN, or partial ARN to update. */
    @get:Input
    abstract val functionName: Property<String>

    /** Path to the deployment package zip file to upload. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val zipFile: RegularFileProperty

    /** Whether to publish a new version after the update. When absent, defaults to `false`. */
    @get:Input
    @get:Optional
    abstract val publish: Property<Boolean>

    @TaskAction
    fun execute() {
        val bytes = zipFile.get().asFile.readBytes()
        val request = UpdateFunctionCodeRequest.builder().apply {
            functionName(functionName.get())
            zipFile(SdkBytes.fromByteArray(bytes))
            if (publish.isPresent) {
                publish(publish.get())
            }
        }.build()
        service.get().getClient().updateFunctionCode(request)
    }
}
