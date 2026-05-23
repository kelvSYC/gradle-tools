package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * [DefaultTask] that downloads multiple assets from a CodeArtifact generic repository concurrently via the
 * Gradle Worker API, wired to a [CodeArtifactClientBuildService].
 *
 * Register artifacts via [registerArtifact] and wire output files downstream with [outputFileForArtifact]:
 *
 * ```kotlin
 * val downloadAll = tasks.register<BatchGetGenericPackageVersionAsset>("downloadAll") {
 *     service.set(codeartifact)
 *     registerArtifact("sdk") {
 *         domain.set("my-domain")
 *         domainOwner.set("111122223333")
 *         repository.set("my-repo")
 *         namespace.set("my-ns")
 *         packageValue.set("my-sdk")
 *         packageVersion.set("1.0.0")
 *         assetName.set("my-sdk-1.0.0.zip")
 *         outputFile.set(layout.buildDirectory.file("downloads/my-sdk-1.0.0.zip"))
 *     }
 * }
 * val sdkZip: Provider<RegularFile> = downloadAll.flatMap { it.outputFileForArtifact("sdk") }
 * ```
 *
 * For BYO-service usage (no auto-registration), extend [AbstractWorkerBatchGetGenericPackageVersionAsset]
 * and set `service` directly.
 *
 * For an async-client-based approach, use [AsyncBatchGetGenericPackageVersionAsset].
 *
 * @see AbstractWorkerBatchGetGenericPackageVersionAsset
 * @see AsyncBatchGetGenericPackageVersionAsset
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchGetGenericPackageVersionAsset @Inject constructor(
    objects: ObjectFactory,
    workerExecutor: WorkerExecutor,
) : AbstractWorkerBatchGetGenericPackageVersionAsset(objects, workerExecutor) {

    /**
     * The shared build service managing the synchronous CodeArtifact client.
     */
    @get:ServiceReference
    abstract override val service: Property<CodeArtifactClientBuildService>
}
