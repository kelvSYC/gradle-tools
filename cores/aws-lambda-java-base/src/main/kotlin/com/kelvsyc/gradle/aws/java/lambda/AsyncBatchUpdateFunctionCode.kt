package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import javax.inject.Inject

/**
 * [DefaultTask] that updates the deployment package for multiple Lambda functions concurrently via
 * `CompletableFuture`, wired to a [LambdaAsyncClientBuildService].
 *
 * All updates are initiated concurrently. Use [waitForActive] (default `true`) to control whether the task
 * waits for each function to reach `LastUpdateStatus = Successful` before completing. Configure
 * [versionArnsFile] to capture published version ARNs as a JSON file for downstream tasks:
 *
 * ```kotlin
 * tasks.register<AsyncBatchUpdateFunctionCode>("updateAll") {
 *     service.set(lambdaAsync)
 *     registerArtifact("api") {
 *         it.functionName.set("my-api-fn")
 *         it.zipFile.set(layout.buildDirectory.file("dist/api.zip"))
 *         it.publish.set(true)
 *     }
 *     registerArtifact("worker") {
 *         it.functionName.set("my-worker-fn")
 *         it.zipFile.set(layout.buildDirectory.file("dist/worker.zip"))
 *         it.publish.set(true)
 *     }
 *     versionArnsFile.set(layout.buildDirectory.file("lambda/version-arns.json"))
 * }
 * ```
 *
 * For BYO-client usage, extend [AbstractAsyncBatchUpdateFunctionCode] and set `client` directly.
 * For sequential updates using the synchronous client, use [BatchUpdateFunctionCode].
 *
 * @see AbstractAsyncBatchUpdateFunctionCode
 * @see BatchUpdateFunctionCode
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class AsyncBatchUpdateFunctionCode @Inject constructor(
    objects: ObjectFactory,
) : AbstractAsyncBatchUpdateFunctionCode(objects) {

    /** The shared build service managing the asynchronous Lambda client. */
    @get:ServiceReference
    abstract val service: Property<LambdaAsyncClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
