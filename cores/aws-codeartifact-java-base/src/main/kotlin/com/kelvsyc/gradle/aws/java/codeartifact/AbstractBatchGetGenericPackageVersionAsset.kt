package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.services.codeartifact.model.GetPackageVersionAssetRequest
import software.amazon.awssdk.services.codeartifact.model.PackageFormat
import javax.inject.Inject

/**
 * Abstract root [DefaultTask] that downloads multiple assets from a CodeArtifact generic repository.
 *
 * Handles artifact registration and request building. Subclasses provide their own `@TaskAction` to
 * implement the actual download concurrency model using the pre-built requests.
 *
 * **Choosing a subclass:**
 * - Extend [AbstractWorkerBatchGetGenericPackageVersionAsset] (or use [BatchGetGenericPackageVersionAsset])
 *   for a Worker API-based approach using the synchronous `CodeartifactClient`. Each artifact is submitted
 *   as a separate worker action; Gradle's worker executor manages concurrency.
 * - Extend [AbstractAsyncBatchGetGenericPackageVersionAsset] (or use [AsyncBatchGetGenericPackageVersionAsset])
 *   for a `CompletableFuture`-based approach using the asynchronous `CodeartifactAsyncClient`. All futures
 *   run concurrently and are joined at the end.
 *
 * **BYO-client use:** Extend one of the abstract mid-level classes and set the `service` or `client`
 * property directly rather than using the service-wired concrete classes.
 *
 * **Wiring output files:**
 * ```kotlin
 * val sdkZip: Provider<RegularFile> = downloadAll.flatMap { it.outputFileForArtifact("sdk") }
 * ```
 *
 * @see AbstractWorkerBatchGetGenericPackageVersionAsset
 * @see AbstractAsyncBatchGetGenericPackageVersionAsset
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AbstractBatchGetGenericPackageVersionAsset @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {

    /**
     * Per-artifact coordinates and output location for a CodeArtifact generic package version asset.
     */
    interface Artifact {
        /** The CodeArtifact domain name. */
        @get:Input
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        @get:Input
        val domainOwner: Property<String>

        /** The CodeArtifact repository name. */
        @get:Input
        val repository: Property<String>

        /** The package namespace. */
        @get:Input
        val namespace: Property<String>

        /** The package name. */
        @get:Input
        val packageValue: Property<String>

        /** The package version. */
        @get:Input
        val packageVersion: Property<String>

        /** The asset name within the package version. */
        @get:Input
        val assetName: Property<String>

        /** The location the asset is to be downloaded to. */
        @get:OutputFile
        val outputFile: RegularFileProperty
    }

    /** Map of artifact name to per-artifact configuration. */
    @get:Nested
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers an artifact to download.
     *
     * @param name Unique name for this artifact within the batch (used in log messages and error reporting).
     * @param action Configures the artifact's coordinates and output file.
     */
    fun registerArtifact(name: String, action: Action<in Artifact>) {
        val artifact = objects.newInstance<Artifact>().also { action.execute(it) }
        artifacts.put(name, artifact)
    }

    /**
     * Returns a [Provider] resolving to the output file for the named artifact.
     *
     * Use this to wire the downloaded file as an input to a downstream task without forcing evaluation.
     */
    fun outputFileForArtifact(name: String): Provider<RegularFile> =
        artifacts.getting(name).flatMap { it.outputFile }

    internal data class Request(
        val name: String,
        val request: GetPackageVersionAssetRequest,
        val outputFile: RegularFileProperty,
    )

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map {
        it.map { (artifactName, artifact) ->
            val request = GetPackageVersionAssetRequest.builder().apply {
                domain(artifact.domain.get())
                domainOwner(artifact.domainOwner.get())
                repository(artifact.repository.get())
                format(PackageFormat.GENERIC)
                namespace(artifact.namespace.get())
                packageValue(artifact.packageValue.get())
                packageVersion(artifact.packageVersion.get())
                asset(artifact.assetName.get())
            }.build()
            Request(artifactName, request, artifact.outputFile)
        }
    }

}
