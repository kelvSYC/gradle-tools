package com.kelvsyc.gradle.aws.kotlin.lambda

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * [DefaultTask] that updates the deployment package for multiple Lambda functions in parallel,
 * wired to a [LambdaClientBuildService].
 *
 * Register functions via [registerArtifact]. Use [waitForActive] (default `true`) to control whether
 * the task waits for each function to reach `LastUpdateStatus = Successful` before completing.
 * Configure [versionArnsFile] to capture published version ARNs as a JSON file for downstream tasks:
 *
 * ```kotlin
 * val updateAll = tasks.register<BatchUpdateFunctionCode>("updateAll") {
 *     service.set(lambda)
 *     registerArtifact("api") {
 *         functionName.set("my-api-fn")
 *         zipFile.set(layout.buildDirectory.file("dist/api.zip"))
 *         publish.set(true)
 *     }
 *     registerArtifact("worker") {
 *         functionName.set("my-worker-fn")
 *         zipFile.set(layout.buildDirectory.file("dist/worker.zip"))
 *         publish.set(true)
 *     }
 *     versionArnsFile.set(layout.buildDirectory.file("lambda/version-arns.json"))
 * }
 * ```
 *
 * For BYO-client usage (no build service), extend [AbstractBatchUpdateFunctionCode] directly.
 *
 * @see AbstractBatchUpdateFunctionCode
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class BatchUpdateFunctionCode @Inject constructor(
    objects: ObjectFactory,
) : AbstractBatchUpdateFunctionCode(objects) {

    /** The shared build service managing the Lambda client. */
    @get:ServiceReference
    abstract val service: Property<LambdaClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
