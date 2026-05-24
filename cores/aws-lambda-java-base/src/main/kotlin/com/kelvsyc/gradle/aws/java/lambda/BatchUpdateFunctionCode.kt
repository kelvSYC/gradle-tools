package com.kelvsyc.gradle.aws.java.lambda

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * [DefaultTask] that updates the deployment package for multiple Lambda functions sequentially,
 * wired to a [LambdaClientBuildService].
 *
 * Register functions via [registerArtifact]. Use [waitForActive] (default `true`) to control whether
 * the task waits for each function to reach `LastUpdateStatus = Successful` before proceeding.
 * Configure [versionArnsFile] to capture published version ARNs as a JSON file for downstream tasks:
 *
 * ```kotlin
 * tasks.register<BatchUpdateFunctionCode>("updateAll") {
 *     service.set(lambda)
 *     registerArtifact("api") {
 *         it.functionName.set("my-api-fn")
 *         it.zipFile.set(layout.buildDirectory.file("dist/api.zip"))
 *         it.publish.set(true)
 *     }
 *     versionArnsFile.set(layout.buildDirectory.file("lambda/version-arns.json"))
 * }
 * ```
 *
 * For BYO-service usage, extend [AbstractSyncBatchUpdateFunctionCode] and set `service` directly.
 * For concurrent updates using the async client, use [AsyncBatchUpdateFunctionCode].
 *
 * @see AbstractSyncBatchUpdateFunctionCode
 * @see AsyncBatchUpdateFunctionCode
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class BatchUpdateFunctionCode @Inject constructor(
    objects: ObjectFactory,
) : AbstractSyncBatchUpdateFunctionCode(objects) {

    /** The shared build service managing the synchronous Lambda client. */
    @get:ServiceReference
    abstract override val service: Property<LambdaClientBuildService>
}
