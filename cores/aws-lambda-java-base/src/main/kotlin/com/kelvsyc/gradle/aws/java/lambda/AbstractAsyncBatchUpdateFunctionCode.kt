package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest
import software.amazon.awssdk.services.lambda.model.UpdateFunctionCodeRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Abstract [AbstractBatchUpdateFunctionCode] that uses a [LambdaAsyncClient] to update multiple Lambda
 * functions concurrently via `CompletableFuture`.
 *
 * All updates are initiated immediately. When [waitForActive] is `true` (the default), each update future
 * is chained to a [software.amazon.awssdk.services.lambda.waiters.LambdaAsyncWaiter] call that polls until
 * `LastUpdateStatus` reaches `Successful`. All futures are joined at the end of the task action. There is
 * no built-in retry support — failures propagate immediately. If any future fails, the task throws.
 *
 * **BYO-client use:** Extend this class and set [client] directly. For automatic service registration via
 * `@ServiceReference`, extend [AsyncBatchUpdateFunctionCode] instead.
 *
 * @see AsyncBatchUpdateFunctionCode
 * @see AbstractSyncBatchUpdateFunctionCode
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class AbstractAsyncBatchUpdateFunctionCode @Inject constructor(
    objects: ObjectFactory,
) : AbstractBatchUpdateFunctionCode(objects) {

    /**
     * The asynchronous Lambda client.
     * Set directly for BYO-client usage; [AsyncBatchUpdateFunctionCode] wires this from a build service.
     */
    @get:Internal
    abstract val client: Property<LambdaAsyncClient>

    @Suppress("detekt:SpreadOperator")
    @TaskAction
    fun run() {
        val lambdaClient = client.get()
        val waiter = if (waitForActive.getOrElse(true)) lambdaClient.waiter() else null
        val versionArns = ConcurrentHashMap<String, String>()

        val futures = requests.getOrElse(emptyList()).map { req ->
            val bytes = req.zipFile.get().asFile.readBytes()
            val request = UpdateFunctionCodeRequest.builder()
                .functionName(req.functionName)
                .zipFile(SdkBytes.fromByteArray(bytes))
                .also { if (req.publish != null) it.publish(req.publish) }
                .build()
            lambdaClient.updateFunctionCode(request)
                .thenCompose { response ->
                    if (waiter != null) {
                        waiter.waitUntilFunctionUpdatedV2(
                            GetFunctionRequest.builder().functionName(req.functionName).build(),
                        ).thenApply { response }
                    } else {
                        CompletableFuture.completedFuture(response)
                    }
                }
                .thenApply { response ->
                    if (req.publish == true) {
                        response.functionArn()?.let { versionArns[req.functionName] = it }
                    }
                }
        }

        CompletableFuture.allOf(*futures.toTypedArray<CompletableFuture<Unit>>()).join()

        if (versionArnsFile.isPresent) {
            writeVersionArnsJson(versionArnsFile.get().asFile, versionArns)
        }
    }
}
