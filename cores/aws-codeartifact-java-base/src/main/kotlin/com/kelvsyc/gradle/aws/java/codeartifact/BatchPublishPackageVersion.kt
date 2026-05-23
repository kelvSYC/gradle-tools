package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * [DefaultTask] that publishes multiple assets to a CodeArtifact generic repository concurrently via the
 * Gradle Worker API, wired to a [CodeArtifactClientBuildService].
 *
 * Register artifacts via [registerArtifact]:
 *
 * ```kotlin
 * tasks.register<BatchPublishPackageVersion>("publishAll") {
 *     service.set(codeartifact)
 *     registerArtifact("jar") {
 *         domain.set("my-domain")
 *         domainOwner.set("111122223333")
 *         repository.set("my-repo")
 *         namespace.set("my-ns")
 *         packageValue.set("my-lib")
 *         packageVersion.set("1.0.0")
 *         assetName.set("my-lib-1.0.0.jar")
 *         assetSHA256.set("abc123...")
 *         assetContent.set(layout.buildDirectory.file("libs/my-lib-1.0.0.jar"))
 *         unfinished.set(true)
 *     }
 *     registerArtifact("sources") {
 *         domain.set("my-domain")
 *         domainOwner.set("111122223333")
 *         repository.set("my-repo")
 *         namespace.set("my-ns")
 *         packageValue.set("my-lib")
 *         packageVersion.set("1.0.0")
 *         assetName.set("my-lib-1.0.0-sources.jar")
 *         assetSHA256.set("def456...")
 *         assetContent.set(layout.buildDirectory.file("libs/my-lib-1.0.0-sources.jar"))
 *     }
 * }
 * ```
 *
 * For BYO-service usage (no auto-registration), extend [AbstractWorkerBatchPublishPackageVersion]
 * and set `service` directly.
 *
 * For an async-client-based approach, use [AsyncBatchPublishPackageVersion].
 *
 * @see AbstractWorkerBatchPublishPackageVersion
 * @see AsyncBatchPublishPackageVersion
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class BatchPublishPackageVersion @Inject constructor(
    objects: ObjectFactory,
    workerExecutor: WorkerExecutor,
) : AbstractWorkerBatchPublishPackageVersion(objects, workerExecutor) {

    /**
     * The shared build service managing the synchronous CodeArtifact client.
     */
    @get:ServiceReference
    abstract override val service: Property<CodeArtifactClientBuildService>
}
