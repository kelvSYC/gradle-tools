package com.kelvsyc.gradle.aws.kotlin.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * [DefaultTask] that downloads multiple assets from a CodeArtifact generic repository in parallel,
 * wired to a [CodeArtifactClientBuildService].
 *
 * Register artifacts via [registerArtifact] and wire output files downstream with [outputFileForArtifact]:
 *
 * ```kotlin
 * val downloadAll = tasks.register<BatchGetGenericPackageVersionAsset>("downloadAll") {
 *     service.set(codeArtifact)
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
 * For BYO-client usage (no build service), extend [AbstractBatchGetGenericPackageVersionAsset] directly.
 *
 * @see AbstractBatchGetGenericPackageVersionAsset
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchGetGenericPackageVersionAsset @Inject constructor(
    objects: ObjectFactory,
) : AbstractBatchGetGenericPackageVersionAsset(objects) {

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
