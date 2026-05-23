package com.kelvsyc.gradle.aws.kotlin.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * [DefaultTask] that publishes multiple assets to a CodeArtifact generic repository in parallel,
 * wired to a [CodeArtifactClientBuildService].
 *
 * Register artifacts via [registerArtifact]:
 *
 * ```kotlin
 * tasks.register<BatchPublishPackageVersion>("publishAll") {
 *     service.set(codeArtifact)
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
 *         unfinished.set(true)  // more assets to come
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
 *         // unfinished absent — marks version as finished
 *     }
 * }
 * ```
 *
 * For BYO-client usage (no build service), extend [AbstractBatchPublishPackageVersion] directly.
 *
 * @see AbstractBatchPublishPackageVersion
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class BatchPublishPackageVersion @Inject constructor(
    objects: ObjectFactory,
) : AbstractBatchPublishPackageVersion(objects) {

    /**
     * The shared build service managing the CodeArtifact client.
     */
    @get:ServiceReference
    abstract val service: Property<CodeArtifactClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
