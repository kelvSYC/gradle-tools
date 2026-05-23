package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * [DefaultTask] that publishes multiple assets to a CodeArtifact generic repository concurrently via
 * `CompletableFuture`, wired to a [CodeArtifactAsyncClientBuildService].
 *
 * Register artifacts via [registerArtifact]:
 *
 * ```kotlin
 * tasks.register<AsyncBatchPublishPackageVersion>("publishAll") {
 *     service.set(codeartifactAsync)
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
 *     }
 * }
 * ```
 *
 * For BYO-client usage (no auto-registration), extend [AbstractAsyncBatchPublishPackageVersion]
 * and set `client` directly.
 *
 * For a Worker API-based approach using the synchronous client, use [BatchPublishPackageVersion].
 *
 * @see AbstractAsyncBatchPublishPackageVersion
 * @see BatchPublishPackageVersion
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class AsyncBatchPublishPackageVersion @Inject constructor(
    objects: ObjectFactory,
) : AbstractAsyncBatchPublishPackageVersion(objects) {

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
