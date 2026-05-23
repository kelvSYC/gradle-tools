package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * [DefaultTask] that downloads multiple assets from a CodeArtifact generic repository concurrently via
 * `CompletableFuture`, wired to a [CodeArtifactAsyncClientBuildService].
 *
 * Register artifacts via [registerArtifact] and wire output files downstream with [outputFileForArtifact]:
 *
 * ```kotlin
 * val downloadAll = tasks.register<AsyncBatchGetGenericPackageVersionAsset>("downloadAll") {
 *     service.set(codeartifactAsync)
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
 * For BYO-client usage (no auto-registration), extend [AbstractAsyncBatchGetGenericPackageVersionAsset]
 * and set `client` directly.
 *
 * For a Worker API-based approach using the synchronous client, use [BatchGetGenericPackageVersionAsset].
 *
 * @see AbstractAsyncBatchGetGenericPackageVersionAsset
 * @see BatchGetGenericPackageVersionAsset
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AsyncBatchGetGenericPackageVersionAsset @Inject constructor(
    objects: ObjectFactory,
) : AbstractAsyncBatchGetGenericPackageVersionAsset(objects) {

    /**
     * The shared build service managing the asynchronous CodeArtifact client.
     */
    @get:ServiceReference
    abstract val service: Property<CodeArtifactAsyncClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
