package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest
import javax.inject.Inject

/**
 * Abstract [AbstractBatchUpdateFunctionCode] that uses a synchronous [LambdaClientBuildService] to update
 * multiple Lambda functions sequentially.
 *
 * Functions are updated one at a time in registration order. When [waitForActive] is `true` (the default),
 * a [software.amazon.awssdk.services.lambda.waiters.LambdaWaiter] is used to poll each function until its
 * `LastUpdateStatus` reaches `Successful` before proceeding to the next. There is no built-in retry
 * support — failures propagate immediately and stop the remaining updates.
 *
 * **BYO-service use:** Extend this class and set [service] directly. For automatic service registration via
 * `@ServiceReference`, extend [BatchUpdateFunctionCode] instead.
 *
 * @see BatchUpdateFunctionCode
 * @see AbstractAsyncBatchUpdateFunctionCode
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class AbstractSyncBatchUpdateFunctionCode @Inject constructor(
    objects: ObjectFactory,
) : AbstractBatchUpdateFunctionCode(objects) {

    /**
     * The build service managing the synchronous Lambda client.
     * Set directly for BYO-service usage; [BatchUpdateFunctionCode] wires this via `@ServiceReference`.
     */
    @get:Internal
    abstract val service: Property<LambdaClientBuildService>

    @TaskAction
    fun run() {
        val lambdaClient = service.get().getClient()
        val waiter = if (waitForActive.getOrElse(true)) lambdaClient.waiter() else null
        val versionArns = mutableMapOf<String, String>()

        requests.getOrElse(emptyList()).forEach { req ->
            val bytes = req.zipFile.get().asFile.readBytes()
            val request = UpdateFunctionCodeRequest.builder()
                .functionName(req.functionName)
                .zipFile(SdkBytes.fromByteArray(bytes))
                .also { if (req.publish != null) it.publish(req.publish) }
                .build()
            val response = lambdaClient.updateFunctionCode(request)
            waiter?.waitUntilFunctionUpdatedV2(
                GetFunctionRequest.builder().functionName(req.functionName).build(),
            )
            if (req.publish == true) {
                response.functionArn()?.let { versionArns[req.functionName] = it }
            }
        }

        if (versionArnsFile.isPresent) {
            writeVersionArnsJson(versionArnsFile.get().asFile, versionArns)
        }
    }
}
